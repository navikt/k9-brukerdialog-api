package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Pleietrengende
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.gyldigPILSSøknad
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class PleiepengerLivetsSluttfaseTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(PleiepengerLivetsSluttfaseTest::class.java)
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
            kafkaEnvironment.stop()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`(){
        val vedlegg = URL(engine.jpegUrl(jwtToken = tokenXToken))
        val søknad = gyldigPILSSøknad(vedleggUrls = listOf(vedlegg), opplastetIdVedleggUrls = listOf(vedlegg))
        requestAndAssert(
            HttpMethod.Post,
            PLEIEPENGER_LIVETS_SLUTTFASE_URL+ INNSENDING_URL,
            søknad.somJson(),
            null,
            HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            engine = engine,
            logger = logger
        )
        kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE).also {
            assertEquals(
                søknad.somKomplettSøknad(søker, søknad.somK9Format(søker)),
                it.data.somPleiepengerLivetsSluttfaseKomplettSøknad()
            )
        }
    }

    @Test
    fun `Innsending av søknad med valideringsfeil`(){
        val vedlegg = URL(engine.jpegUrl(jwtToken = tokenXToken))
        val søknad = gyldigPILSSøknad(
            vedleggUrls = listOf(vedlegg),
            opplastetIdVedleggUrls = listOf(vedlegg),
            pleietrengende = Pleietrengende(norskIdentitetsnummer = "123", navn = " "),
        )
        requestAndAssert(
            HttpMethod.Post,
            PLEIEPENGER_LIVETS_SLUTTFASE_URL+ INNSENDING_URL,
            søknad.somJson(),
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    "pleietrengende.navn kan ikke være tomt eller blankt.",
                    "pleietrengende.norskIdentitetsnummer er ikke gyldig identifikator, '123*****'. Forventet at personidentifikator kun var siffer, men var 123****** (3)"
                  ]
                }
            """.trimIndent(),
            HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            engine = engine,
            logger = logger
        )
    }
}