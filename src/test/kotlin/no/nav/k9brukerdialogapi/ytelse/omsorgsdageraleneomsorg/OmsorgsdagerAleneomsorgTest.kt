package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.TestUtils.Companion.issueToken
import no.nav.helse.TestUtils.Companion.requestAndAssert
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9BrukerdialogCache
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.TidspunktForAleneomsorg
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.TypeBarn
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgsdagerAleneomsorgTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(OmsorgsdagerAleneomsorgTest::class.java)
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
            kafkaEnvironment.tearDown()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`() {
        val søknad = Søknad(
            barn = listOf(
                Barn(
                    navn = "Barn1",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "123",
                    identitetsnummer = "25058118020",
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                )
            ),
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = OMSORGSDAGER_ALENEOMSORG_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson(),
            engine = engine,
            logger = logger
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.OMSORGSDAGER_ALENEOMSORG)
        assertEquals(
            søknad.somKomplettSøknad(SøknadUtils.søker),
            hentet.data.somOmsorgsdagerAleneomsorgKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad gir valideringsfeil`() {
        val søknad = Søknad(
            barn = listOf(
                Barn(
                    navn = " ",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "123",
                    identitetsnummer = null,
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                    dato = null
                )
            ),
            språk = "nb",
            harForståttRettigheterOgPlikter = false,
            harBekreftetOpplysninger = false
        )
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSDAGER_ALENEOMSORG_URL + INNSENDING_URL,
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
                    "harBekreftetOpplysninger må være true",
                    "harForståttRettigheterOgPlikter må være true",
                    "barn[0].navn kan ikke være tomt/blankt.",
                    "barn[0].identitetsnummer kan ikke være null eller blank.",
                    "barn[0].dato må være satt."
                  ],
                  "status": 400
                }
                """.trimIndent()
        )
    }
}