package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.prometheus.client.CollectorRegistry
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.KafkaWrapper
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_SNF_URL
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.jpegUrl
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.somOmsorgspengerUtbetalingSnfKomplettSøknad
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.genererSøknadForOmsUtSnf
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgspengerUtbetalingSnfTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtbetalingSnfTest::class.java)
        val mockOAuth2Server = MockOAuth2Server().apply { start() }
        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .withTokendingsSupport()
            .k9BrukerdialogApiConfig()
            .build()
            .stubK9Mellomlagring()
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaKonsumer = kafkaEnvironment.testConsumer()

        private val gyldigFødselsnummerA = "02119970078"
        private val tokenXToken = mockOAuth2Server.issueToken(fnr = gyldigFødselsnummerA)

        fun getConfig(): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment,
                    mockOAuth2Server = mockOAuth2Server
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }

        val engine = TestApplicationEngine(createTestEnvironment { config = getConfig() })

        @BeforeAll
        @JvmStatic
        fun buildUp() {
            CollectorRegistry.defaultRegistry.clear()
            engine.start(wait = true)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wireMockServer.stop()
            mockOAuth2Server.shutdown()
            kafkaEnvironment.stop()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`() {
        val vedlegg = URL(engine.jpegUrl(jwtToken = tokenXToken))
        val søknad = genererSøknadForOmsUtSnf(vedlegg = listOf(vedlegg))
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_UTBETALING_SNF_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson()
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId.id, Ytelse.OMSORGSPENGER_UTBETALING_SNF)
        assertEquals(
            søknad.somKomplettSøknad(SøknadUtils.søker, søknad.somK9Format(SøknadUtils.søker, metadata)),
            hentet.data.somOmsorgspengerUtbetalingSnfKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad med flere valideringsfeil`() {
        val søknad = genererSøknadForOmsUtSnf(
            utbetalingsperiode = listOf(
                Utbetalingsperiode(
                    fraOgMed = LocalDate.parse("2022-01-20"),
                    tilOgMed = LocalDate.parse("2022-01-19"),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf()
                ),
                Utbetalingsperiode(
                    fraOgMed = LocalDate.parse("2022-01-20"),
                    tilOgMed = LocalDate.parse("2022-01-25"),
                    antallTimerPlanlagt = Duration.ofHours(5),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
                )
            ),
            opphold = listOf(
                Opphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-01"),
                    landkode = " ",
                    landnavn = "Nederland",
                    erEØSLand = null
                )
            ),
            barn = listOf(
                Barn(
                    navn = "Barnesen",
                    fødselsdato = LocalDate.now().minusYears(14),
                    type = TypeBarn.FRA_OPPSLAG,
                    utvidetRett = false,
                    identitetsnummer = "26104500284"
                )
            )
        )
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_UTBETALING_SNF_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
                {
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "invalid_parameters": [
                    "Hvis alle barna er 13 år eller eldre må minst et barn ha utvidet rett.",
                    "opphold[0].erEØSLand må være satt",
                    "opphold[0].fraOgMed kan ikke være etter tilOgMed",
                    "opphold[0].landkode kan ikke være blankt eller tomt. landkode=' '",
                    "utbetalingsperioder[0].aktivitetFravær kan ikke være tom.",
                    "utbetalingsperioder[0].tilOgMed må være lik eller etter fraOgMed.",
                    "utbetalingsperioder[1].Dersom antallTimerPlanlagt er satt må antallTimerBorte være satt"
                  ],
                  "status": 400
                }
            """.trimIndent(),
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson()
        )
    }

}
