package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.TestUtils.Companion.issueToken
import no.nav.helse.TestUtils.Companion.requestAndAssert
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Arbeidssituasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Fordele
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Melding
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Meldingstype.FORDELING
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.SAMVÆRSFORELDER
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgsdagerMeldingTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(OmsorgsdagerMeldingTest::class.java)
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
            .stubK9Mellomlagring()

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
    fun `Innsending av gyldig melding om fordeling`() {
        val melding = Melding(
            id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
            språk = "nb",
            mottakerFnr = "26104500284",
            mottakerNavn = "Navnesen",
            barn = listOf(
                Barn(
                    identitetsnummer = "02119970078",
                    fødselsdato = LocalDate.now(),
                    navn = "Navnesen",
                    aleneOmOmsorgen = true,
                    utvidetRett = true
                )
            ),
            harUtvidetRett = true,
            harAleneomsorg = true,
            erYrkesaktiv = true,
            arbeiderINorge = true,
            arbeidssituasjon = listOf(Arbeidssituasjon.ARBEIDSTAKER),
            type = FORDELING,
            fordeling = Fordele(
                mottakerType = SAMVÆRSFORELDER, samværsavtale = listOf(URL(engine.jpegUrl(jwtToken = tokenXToken)))
            ),
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "$OMSORGSDAGER_MELDING_URL$INNSENDING_URL",
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = melding.somJson(),
            engine = engine,
            logger = logger
        )
        val hentet = kafkaKonsumer.hentSøknad(melding.søknadId, Ytelse.OMSORGSDAGER_MELDING_FORDELING)
        assertEquals(
            melding.somKomplettMelding(søker),
            hentet.data.somOmsorgsdagerKomplettMelding()
        )
    }

    @Test
    fun `Innsending av ugyldig melding gir valideringsfeil`() {
        val melding = Melding(
            id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
            språk = "nb",
            mottakerFnr = "26104500284",
            mottakerNavn = "Navnesen",
            barn = listOf(),
            harUtvidetRett = null,
            harAleneomsorg = null,
            erYrkesaktiv = null,
            arbeiderINorge = null,
            arbeidssituasjon = listOf(Arbeidssituasjon.ARBEIDSTAKER),
            type = FORDELING,
            fordeling = null,
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "$OMSORGSDAGER_MELDING_URL$INNSENDING_URL",
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            requestEntity = melding.somJson(),
            expectedResponse = """
                {
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "invalid_parameters": [
                    "barn kan ikke være en tom liste.",
                    "Ved type=FORDELING kan ikke 'fordeling' være null.",
                    "erYrkesaktiv kan ikke være null. Må være true/false.",
                    "arbeiderINorge kan ikke være null. Må være true/false.",
                    "harAleneomsorg kan ikke være null. Må være true/false.",
                    "harUtvidetRett kan ikke være null. Må være true/false."
                  ],
                  "status": 400
                }
            """.trimIndent(),
            engine = engine,
            logger = logger
        )
    }
}