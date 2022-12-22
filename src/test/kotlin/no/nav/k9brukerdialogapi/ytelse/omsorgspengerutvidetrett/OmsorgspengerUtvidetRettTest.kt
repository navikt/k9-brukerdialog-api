package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

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
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTVIDET_RETT_URL
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.somOmsorgspengerUtvidetRettKomplettSøknad
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnSøknad
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.SøkerBarnRelasjon
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgspengerUtvidetRettTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtvidetRettTest::class.java)
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
        val søknad = OmsorgspengerKroniskSyktBarnSøknad(
            språk = "nb",
            kroniskEllerFunksjonshemming = true,
            barn = Barn(
                norskIdentifikator = null,
                fødselsdato = null,
                aktørId = "1000000000001",
                navn = "Barn Barnesen"
            ),
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            sammeAdresse = true,
            legeerklæring = listOf(),
            samværsavtale = listOf(),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_UTVIDET_RETT_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson()
        )
        val hentet = kafkaKonsumer.hentSøknad(søknad.søknadId, Ytelse.OMSORGSPENGER_UTVIDET_RETT)
        assertEquals(
            søknad.somKomplettSøknad(SøknadUtils.søker, søknad.somK9Format(SøknadUtils.søker)),
            hentet.data.somOmsorgspengerUtvidetRettKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad som får valideringsfeil`() {
        val søknad = OmsorgspengerKroniskSyktBarnSøknad(
            språk = "nb",
            kroniskEllerFunksjonshemming = true,
            barn = Barn(
                norskIdentifikator = "123",
                navn = "Barn Barnesen"
            ),
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            sammeAdresse = true,
            legeerklæring = listOf(),
            samværsavtale = listOf(),
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = false
        ).somJson()

        requestAndAssert(
            engine = engine,
            logger = logger,
            httpMethod = HttpMethod.Post,
            path = OMSORGSPENGER_UTVIDET_RETT_URL + INNSENDING_URL,
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            expectedResponse = """
                {
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "invalid_parameters": [
                    "harBekreftetOpplysninger må være true",
                    "harForståttRettigheterOgPlikter må være true",
                    "barn.norskIdentifikator er ikke gyldig identifikator, '123*****'. Forventet at personidentifikator kun var siffer, men var 123****** (3)"
                  ],
                  "status": 400
                }
            """.trimIndent(),
            requestEntity = søknad
        )
    }

}
