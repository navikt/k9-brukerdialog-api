package no.nav.k9brukerdialogapi.ytelse.ettersending

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.wiremock.*
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class EttersendingTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(EttersendingTest::class.java)
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
            .stubK9BrukerdialogCache()

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
        val søknad = Ettersendelse(
            språk = "nb",
            vedlegg = setOf<URL>(vedlegg).toList(),
            beskrivelse = "Sykt barn...",
            søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        TestUtils.requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = ETTERSENDING_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            expectedResponse = null,
            requestEntity = søknad.somJson()
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.ETTERSENDING)
        assertEquals(
            søknad.somKomplettSøknad(SøknadUtils.søker, søknad.somK9Format(SøknadUtils.søker), listOf("nav-logo.png")),
            hentet.data.somEttersendingKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad som får valideringsfeil`() {
        val søknad = Ettersendelse(
            språk = "nb",
            vedlegg = listOf(),
            søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
            beskrivelse = null,
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = false
        )

        TestUtils.requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = ETTERSENDING_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            expectedResponse = """
                {
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "invalid_parameters": [
                    "harForståttRettigheterOgPlikter må være true",
                    "harBekreftetOpplysninger må være true",
                    "Liste over vedlegg kan ikke være tom",
                    "beskrivelse må være satt dersom det gjelder pleiepenger"
                  ],
                  "status": 400
                }
            """.trimIndent(),
            requestEntity = søknad.somJson()
        )
    }

}
