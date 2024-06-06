package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

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
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.jpegUrl
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.somOmsorgspengerUtbetalingArbeidstakerKomplettSøknad
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.*
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
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
            kafkaEnvironment.stop()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`() {
        val vedlegg = URI.create(engine.jpegUrl(jwtToken = tokenXToken)).toURL()
        val søknad = OmsorgspengerutbetalingArbeidstakerSøknad(
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
                            fraOgMed = LocalDate.now().minusDays(1),
                            tilOgMed = LocalDate.now(),
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                        )
                    )
                )
            ),
            dineBarn = DineBarn(
                harDeltBosted = false,
                barn = listOf(
                    Barn(
                        identitetsnummer = "11223344567",
                        aktørId = "1234567890",
                        LocalDate.now(),
                        "Barn Barnesen",
                        TypeBarn.FRA_OPPSLAG
                    )
                ),
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
            requestEntity = søknad.somJson(),
            ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER)
        assertEquals(
            søknad.somKomplettSøknad(SøknadUtils.søker, søknad.somK9Format(SøknadUtils.søker, metadata), listOf("vedlegg1")),
            hentet.data.somOmsorgspengerUtbetalingArbeidstakerKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad med flere valideringsfeil`() {
        val søknad = OmsorgspengerutbetalingArbeidstakerSøknad(
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
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                        )
                    )
                )
            ),
            dineBarn = DineBarn(
                harDeltBosted = false,
                barn = listOf(
                    Barn(
                        identitetsnummer = "11223344567",
                        aktørId = "1234567890",
                        LocalDate.now(),
                        "Barn Barnesen",
                        TypeBarn.FRA_OPPSLAG
                    )
                ),
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
            requestEntity = søknad.somJson(),
            ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
        )
    }

}
