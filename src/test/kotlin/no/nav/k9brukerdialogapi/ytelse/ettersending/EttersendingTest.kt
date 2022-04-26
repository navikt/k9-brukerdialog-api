package no.nav.k9brukerdialogapi.ytelse.ettersending

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.TestUtils
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.wiremock.*
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
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
        private val tokenXToken = TestUtils.getTokenDingsToken(fnr = gyldigFødselsnummerA)

        fun getConfig(): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment
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
            kafkaEnvironment.tearDown()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`() {
        val vedlegg = URL(engine.jpegUrl(jwtToken = tokenXToken))
        val søknad = Søknad(
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
        val søknad = Søknad(
            språk = "nb",
            vedlegg = listOf(),
            søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
            beskrivelse = null,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
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
                        {
                          "name": "vedlegg",
                          "reason": "Liste over vedlegg kan ikke være tom.",
                          "invalid_value": [],
                          "type": "entity"
                        },
                        {
                          "type": "entity",
                          "name": "beskrivelse",
                          "invalid_value" : null,
                          "reason": "Beskrivelse kan ikke være tom, null eller blank dersom det gjelder pleiepenger."
                        }
                      ],
                      "status": 400
                    }
            """.trimIndent(),
            requestEntity = søknad.somJson()
        )
    }

}