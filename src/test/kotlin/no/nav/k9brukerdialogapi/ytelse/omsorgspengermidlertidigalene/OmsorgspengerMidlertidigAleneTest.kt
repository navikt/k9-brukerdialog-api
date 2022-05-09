package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.TestUtils
import no.nav.helse.TestUtils.Companion.issueToken
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.*
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.AnnenForelder
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Søknad
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
            kafkaEnvironment.tearDown()
        }
    }

    @Test
    fun `Innsending av gyldig søknad`() {
        val søknad = Søknad(
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
        TestUtils.requestAndAssert(
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
            søknad.tilKomplettSøknad(søker, søknad.tilK9Format(søker)),
            hentet.data.somOmsorgspengerMidlertidigAleneKomplettSøknad()
        )
    }

    @Test
    fun `Innsending av ugyldig søknad gir valideringsfeil`() {
        val søknad = Søknad(
            id = "123456789",
            språk = "nb",
            annenForelder = AnnenForelder(
                navn = "Berit",
                fnr = "ikke gyldig",
                situasjon = Situasjon.FENGSEL,
                situasjonBeskrivelse = "Sitter i fengsel..",
                periodeOver6Måneder = false,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = null
            ),
            barn = listOf(
                Barn(
                    navn = "Ole Dole",
                    norskIdentifikator = "ikke gyldig",
                    aktørId = null
                )
            ),
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = true
        )
        TestUtils.requestAndAssert(
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
                {
                  "type": "entity",
                  "name": "harBekreftetOpplysninger",
                  "reason": "Opplysningene må bekreftes for å sende inn søknad.",
                  "invalid_value": null
                },
                {
                  "name": "AnnenForelder.fnr",
                  "reason": "Fødselsnummer på annen forelder må være gyldig norsk identifikator",
                  "invalid_value": "ikke gyldig",
                  "type": "entity"
                },
                {
                  "type": "entity",
                  "name": "AnnenForelder.periodeTilOgMed",
                  "reason": "periodeTilOgMed kan ikke være null dersom situasjonen er FENGSEL",
                  "invalid_value": null
                },
                {
                  "name": "barn.norskIdentifikator",
                  "reason": "Ikke gyldig norskIdentifikator.",
                  "invalid_value": "ikke gyldig",
                  "type": "entity"
                }
              ],
              "status": 400
            }
                """.trimIndent()
        )
    }
}
