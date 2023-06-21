package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene

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
import no.nav.k9brukerdialogapi.OMSORGSPENGER_MIDLERTIDIG_ALENE_URL
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.somOmsorgspengerMidlertidigAleneKomplettSøknad
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.AnnenForelder
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.OmsorgspengerMdlertidigAleneSøknad
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgspengerMidlertidigAleneTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerMidlertidigAleneTest::class.java)
        val mockOAuth2Server = MockOAuth2Server().apply { start() }
        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .withTokendingsSupport()
            .k9BrukerdialogApiConfig()
            .build()
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
        val søknad = OmsorgspengerMdlertidigAleneSøknad(
            id = "123456789",
            språk = "nb",
            annenForelder = AnnenForelder(
                navn = "Berit",
                fnr = "02119970078",
                situasjon = Situasjon.FENGSEL,
                situasjonBeskrivelse = "Sitter i fengsel..",
                periodeOver6Måneder = false,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = LocalDate.parse("2020-10-01")
            ),
            barn = listOf(
                Barn(
                    navn = "Ole Dole",
                    norskIdentifikator = "25058118020",
                    aktørId = null
                )
            ),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_MIDLERTIDIG_ALENE_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson(),
            engine = engine,
            logger = logger
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE)
        assertEquals(
            søknad.somKomplettSøknad(søker, søknad.somK9Format(søker, metadata)),
            hentet.data.somOmsorgspengerMidlertidigAleneKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad gir valideringsfeil`() {
        val søknad = OmsorgspengerMdlertidigAleneSøknad(
            id = "123456789",
            språk = "nb",
            annenForelder = AnnenForelder(
                navn = "Berit",
                fnr = "11111111111",
                situasjon = Situasjon.FENGSEL,
                situasjonBeskrivelse = "Sitter i fengsel..",
                periodeOver6Måneder = false,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = null
            ),
            barn = listOf(
                Barn(
                    navn = "  ",
                    norskIdentifikator = "11111111111",
                    aktørId = null
                )
            ),
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = false
        )
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_MIDLERTIDIG_ALENE_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson(),
            expectedResponse = """
                {
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "invalid_parameters": [
                    "harForståttRettigheterOgPlikter må være true",
                    "harBekreftetOpplysninger må være true",
                    "annenForelder.fnr er ikke gyldig identifikator, '111111*****'. kalkulertKontrollsifferEn (-) er ikke lik forventetKontrollsifferEn (1)",
                    "annenForelder.periodeTilOgMed kan ikke være null dersom situasjonen er FENGSEL eller UTØVER_VERNEPLIKT",
                    "barn[0].norskIdentifikator er ikke gyldig identifikator, '111111*****'. kalkulertKontrollsifferEn (-) er ikke lik forventetKontrollsifferEn (1)",
                    "barn[0].navn kan ikke være tomt eller blank."
                  ],
                  "status": 400
                }
                """.trimIndent()
        )
    }
}
