package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.TestUtils.Companion.issueToken
import no.nav.helse.TestUtils.Companion.requestAndAssert
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.*
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgspengerUtbetalingArbeidstakerTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtbetalingArbeidstakerTest::class.java)
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
            kafkaEnvironment.tearDown()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`() {
        val vedlegg = URL(engine.jpegUrl(jwtToken = tokenXToken))
        val søknad = Søknad(
            språk = "nb",
            vedlegg = listOf(
                vedlegg
            ),
            bosteder = listOf(),
            opphold = listOf(),
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Kiwi AS",
                    organisasjonsnummer = "825905162",
                    utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                    konfliktForklaring = "Fordi blablabla",
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = true,
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = LocalDate.now().minusDays(4),
                            tilOgMed = LocalDate.now(),
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                        )
                    )
                )
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        )
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson()
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER)
        assertEquals(
            søknad.tilKomplettSøknad(SøknadUtils.søker, søknad.tilK9Format(SøknadUtils.søker), listOf("vedlegg1")),
            hentet.data.somOmsorgspengerUtbetalingArbeidstakerKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad med flere valideringsfeil`() {
        val søknad = Søknad(
            språk = "nb",
            vedlegg = listOf(),
            bosteder = listOf(
                Bosted(
                    fraOgMed = LocalDate.now(),
                    tilOgMed = LocalDate.now().minusDays(2),
                    landkode = "IKKE GYLDIG",
                    landnavn = " ",
                    erEØSLand = null
                )
            ),
            opphold = listOf(
                Opphold(
                    fraOgMed = LocalDate.now(),
                    tilOgMed = LocalDate.now().minusDays(2),
                    landkode = "IKKE GYLDIG",
                    landnavn = " ",
                    erEØSLand = null
                )
            ),
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = null,
                harForståttRettigheterOgPlikter = false
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = " ",
                    organisasjonsnummer = "IKKE GYLDIG",
                    utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                    konfliktForklaring = null,
                    harHattFraværHosArbeidsgiver = null,
                    arbeidsgiverHarUtbetaltLønn = null,
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = LocalDate.now().minusDays(4),
                            tilOgMed = LocalDate.now(),
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                        )
                    )
                )
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        )
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
                {
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "invalid_parameters": [
                     "bosteder[0].erEØSLand må være satt",
                     "bosteder[0].fraOgMed kan ikke være etter tilOgMed",
                     "bosteder[0].landnavn kan ikke være blankt eller tomt. landnavn=' '",
                     "opphold[0].erEØSLand må være satt",
                     "opphold[0].fraOgMed kan ikke være etter tilOgMed",
                     "opphold[0].landnavn kan ikke være blankt eller tomt. landnavn=' '",
                     "bekreftelser.harBekreftetOpplysninger må være true",
                     "bekreftelser.harForståttRettigheterOgPlikter må være true",
                     "arbeidsgivere[0].navn kan ikke være blankt eller tomt. navn=' '",
                     "arbeidsgivere[0].arbeidsgiverHarUtbetaltLønn må være satt",
                     "arbeidsgivere[0].harHattFraværHosArbeidsgiver må være satt",
                     "arbeidsgivere[0].konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER"
                  ],
                  "status": 400
                }
            """.trimIndent(),
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson()
        )
    }

}