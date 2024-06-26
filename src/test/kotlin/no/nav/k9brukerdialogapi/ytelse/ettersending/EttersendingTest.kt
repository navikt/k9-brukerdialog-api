package no.nav.k9brukerdialogapi.ytelse.ettersending

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.prometheus.client.CollectorRegistry
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9brukerdialogapi.ETTERSENDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.KafkaWrapper
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.jpegUrl
import no.nav.k9brukerdialogapi.somEttersendingKomplettSøknad
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9BrukerdialogCache
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Pleietrengende
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
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
        val vedlegg = URI.create(engine.jpegUrl(jwtToken = tokenXToken)).toURL()
        val søknad = Ettersendelse(
            språk = "nb",
            vedlegg = setOf<URL>(vedlegg).toList(),
            beskrivelse = "Sykt barn...",
            søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
            ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
            pleietrengende = Pleietrengende(norskIdentitetsnummer = "02119970078"),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        val ytelse = Ytelse.ETTERSENDING
        TestUtils.requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = ETTERSENDING_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            expectedResponse = null,
            requestEntity = søknad.somJson(),
            ytelse = ytelse
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, ytelse)
        assertEquals(
            søknad.somKomplettSøknad(SøknadUtils.søker, søknad.somK9Format(SøknadUtils.søker, metadata), listOf("nav-logo.png")),
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
            ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
            pleietrengende = null,
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
                    "Pleietrengende må være satt dersom ettersendelsen gjelder legeerklæring",
                    "harForståttRettigheterOgPlikter må være true",
                    "harBekreftetOpplysninger må være true",
                    "Liste over vedlegg kan ikke være tom"
                  ],
                  "status": 400
                }
            """.trimIndent(),
            requestEntity = søknad.somJson(),
            ytelse = Ytelse.ETTERSENDING
        )
    }

}
