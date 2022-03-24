package no.nav.k9brukerdialogapi

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.TestUtils.Companion.getAuthCookie
import no.nav.helse.TestUtils.Companion.getTokenDingsToken
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.wiremock.*
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .withTokendingsSupport()
            .k9BrukerdialogApiConfig()
            .build()
            .stubOppslagHealth()
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9Mellomlagring()
            .stubK9OppslagArbeidsgivere()

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaKonsumer = kafkaEnvironment.testConsumer()

        private val gyldigFødselsnummerA = "02119970078"
        private const val ikkeMyndigFnr = "12125012345"
        private val cookie = getAuthCookie(gyldigFødselsnummerA)
        private val tokenXToken = getTokenDingsToken(fnr = gyldigFødselsnummerA)

        val redisServer: RedisServer = RedisServer
            .newRedisServer().apply { start() }

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment,
                    redisServer = redisServer
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }

        val engine = TestApplicationEngine(createTestEnvironment { config = getConfig() })

        @BeforeAll
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            redisServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                System.err.println(response.content)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Nested
    inner class SøkerOppslagTest{

        @Test
        fun `Hente søker med loginservice token som cookie`() {
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL+SØKER_URL,
                expectedCode = HttpStatusCode.OK,
                expectedResponse = """
                    {
                        "etternavn": "MORSEN",
                        "fornavn": "MOR",
                        "mellomnavn": "HEISANN",
                        "fødselsnummer": "$gyldigFødselsnummerA",
                        "aktørId": "12345",
                        "fødselsdato": "1999-11-02"
                    }
                """.trimIndent(),
                cookie = cookie
            )
        }

        @Test
        fun `Hente søker med tokenX token som authorization header`() {
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL + SØKER_URL,
                expectedCode = HttpStatusCode.OK,
                expectedResponse = """
                {
                    "etternavn": "MORSEN",
                    "fornavn": "MOR",
                    "mellomnavn": "HEISANN",
                    "fødselsnummer": "$gyldigFødselsnummerA",
                    "aktørId": "12345",
                    "fødselsdato": "1999-11-02"
                }
            """.trimIndent(),
                jwtToken = tokenXToken
            )
        }

        @Test
        fun `Hente søker som ikke er myndig`() {
            wireMockServer.stubK9OppslagSoker(
                statusCode = HttpStatusCode.fromValue(451),
                responseBody =
                //language=json
                """
            {
                "detail": "Policy decision: DENY - Reason: (NAV-bruker er i live AND NAV-bruker er ikke myndig)",
                "instance": "/meg",
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451
            }
            """.trimIndent()
            )

            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL + SØKER_URL,
                expectedCode = HttpStatusCode.fromValue(451),
                expectedResponse =
                //language=json
                """
            {
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451,
                "instance": "/oppslag/soker",
                "detail": "Tilgang nektet."
            }
            """.trimIndent(),
                cookie = getAuthCookie(ikkeMyndigFnr)
            )

            wireMockServer.stubK9OppslagSoker() // reset til default mapping
        }

        @Test
        fun `Hente søker med tilgangsnivå 3`() {
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL + SØKER_URL,
                cookie = getAuthCookie(fnr = gyldigFødselsnummerA, level = 3),
                expectedCode = HttpStatusCode.Forbidden,
                expectedResponse = null
            )
        }
    }

    @Nested
    inner class MellomlagringTest {

        @Test
        fun `Test flyt av mellomlagring`() {
            // Legge til mellomlagring
            requestAndAssert(
                httpMethod = HttpMethod.Post,
                path = "mellomlagring/OMSORGSPENGER_UTVIDET_RETT",
                expectedCode = HttpStatusCode.NoContent,
                cookie = cookie,
                expectedResponse = null,
                requestEntity = """
                    {
                        "testdata": "test mellomlagring 123"
                    }
                """.trimIndent()
            )

            // Hente mellomlagring
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = "mellomlagring/OMSORGSPENGER_UTVIDET_RETT",
                expectedCode = HttpStatusCode.OK,
                cookie = cookie,
                expectedResponse = """
                    {
                        "testdata": "test mellomlagring 123"
                    }
                """.trimIndent()
            )

            // Oppdatere mellomlagring
            requestAndAssert(
                httpMethod = HttpMethod.Put,
                path = "mellomlagring/OMSORGSPENGER_UTVIDET_RETT",
                expectedCode = HttpStatusCode.NoContent,
                cookie = cookie,
                expectedResponse = null,
                requestEntity = """
                    {
                        "testdata": "test oppdatert mellomlagring 123"
                    }
                """.trimIndent()
            )

            // Hente oppdatert mellomlagring
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = "mellomlagring/OMSORGSPENGER_UTVIDET_RETT",
                expectedCode = HttpStatusCode.OK,
                cookie = cookie,
                expectedResponse = """
                    {
                        "testdata": "test oppdatert mellomlagring 123"
                    }
                """.trimIndent()
            )

            // Slette mellomlagring
            requestAndAssert(
                httpMethod = HttpMethod.Delete,
                path = "mellomlagring/OMSORGSPENGER_UTVIDET_RETT",
                expectedCode = HttpStatusCode.Accepted,
                cookie = cookie,
                expectedResponse = null
            )

            // Hente mellomlagring som skal være slettet
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = "mellomlagring/OMSORGSPENGER_UTVIDET_RETT",
                expectedCode = HttpStatusCode.OK,
                cookie = cookie,
                expectedResponse = "{}"
            )
        }
    }

    @Nested
    inner class BarnOppslagTest {
        @Test
        fun `Hente barn og eksplisit sjekke at identitetsnummer ikke blir med ved get kall`() {
            val respons = requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL+BARN_URL,
                expectedCode = HttpStatusCode.OK,
                cookie = cookie,
                //language=json
                expectedResponse = """
                {
                  "barn": [
                    {
                      "fødselsdato": "2000-08-27",
                      "fornavn": "BARN",
                      "mellomnavn": "EN",
                      "etternavn": "BARNESEN",
                      "aktørId": "1000000000001"
                    },
                    {
                      "fødselsdato": "2001-04-10",
                      "fornavn": "BARN",
                      "mellomnavn": "TO",
                      "etternavn": "BARNESEN",
                      "aktørId": "1000000000002"
                    }
                  ]
                }
            """.trimIndent()
            )

            val responsSomJSONArray = JSONObject(respons).getJSONArray("barn")

            assertFalse(responsSomJSONArray.getJSONObject(0).has("identitetsnummer"))
            assertFalse(responsSomJSONArray.getJSONObject(1).has("identitetsnummer"))
        }

        @Test
        fun `Feil ved henting av barn skal returnere tom liste`() {
            wireMockServer.stubK9OppslagBarn(simulerFeil = true)
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL+BARN_URL,
                expectedCode = HttpStatusCode.OK,
                expectedResponse = """
            {
                "barn": []
            }
            """.trimIndent(),
                cookie = getAuthCookie("26104500284")
            )
            wireMockServer.stubK9OppslagBarn()
        }
    }

    @Nested
    inner class ArbeidsgiverOppslagTest{
        @Test
        fun `Oppslag av alle arbeidsgiver inkludert private og frilansoppdrag`(){
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = "$OPPSLAG_URL$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30&frilansoppdrag=true&private_arbeidsgivere=true",
                expectedCode = HttpStatusCode.OK,
                //language=json
                expectedResponse = """
            {
              "organisasjoner": [
                {
                  "organisasjonsnummer": "913548221",
                  "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                  "ansattFom": null,
                  "ansattTom": null
                },
                {
                  "organisasjonsnummer": "984054564",
                  "navn": "NAV, AVD WALDEMAR THRANES GATE",
                  "ansattFom": null,
                  "ansattTom": null
                }
              ],
              "privateArbeidsgivere": [
                {
                  "offentligIdent": "10047206508",
                  "ansattFom": "2014-07-01",
                  "ansattTom": "2015-12-31"
                }
              ],
              "frilansoppdrag": [
                {
                  "type": "Person",
                  "organisasjonsnummer": null,
                  "navn": null,
                  "offentligIdent": "805824352",
                  "ansattFom": "2020-01-01",
                  "ansattTom": "2022-02-28"
                },
                {
                  "type": "Organisasjon",
                  "organisasjonsnummer": "123456789",
                  "navn": "DNB, FORSIKRING",
                  "offentligIdent": null,
                  "ansattFom": "2020-01-01",
                  "ansattTom": "2022-02-28"
                }
              ]
            }
            """.trimIndent(),
                cookie = cookie
            )
        }

        @Test
        fun `Oppslag av arbeidsgivere uten private og frilansoppdrag`(){
            requestAndAssert(
                httpMethod = HttpMethod.Get,
                path = "$OPPSLAG_URL$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30",
                expectedCode = HttpStatusCode.OK,
                //language=json
                expectedResponse = """
            {
              "organisasjoner": [
                {
                  "organisasjonsnummer": "913548221",
                  "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                  "ansattFom": null,
                  "ansattTom": null
                },
                {
                  "organisasjonsnummer": "984054564",
                  "navn": "NAV, AVD WALDEMAR THRANES GATE",
                  "ansattFom": null,
                  "ansattTom": null
                }
              ],
              "privateArbeidsgivere": null,
              "frilansoppdrag": null
            }
            """.trimIndent(),
                cookie = cookie
            )
        }
    }

    @Nested
    inner class VedleggTest{
        @Test
        fun `Test håndtering av vedlegg`() {
            val jpeg = "vedlegg/iPhone_6.jpg".fromResources().readBytes()

            with(engine) {
                // LASTER OPP VEDLEGG
                val url = handleRequestUploadImage(
                    cookie = cookie,
                    vedlegg = jpeg
                )
                val path = Url(url).fullPath
                // HENTER OPPLASTET VEDLEGG
                handleRequest(HttpMethod.Get, path) {
                    addHeader("Cookie", cookie.toString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(Arrays.equals(jpeg, response.byteContent))
                    // SLETTER OPPLASTET VEDLEGG
                    handleRequest(HttpMethod.Delete, path) {
                        addHeader("Cookie", cookie.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.NoContent, response.status())
                        // VERIFISERER AT VEDLEGG ER SLETTET
                        handleRequest(HttpMethod.Get, path) {
                            addHeader("Cookie", cookie.toString())
                        }.apply {
                            assertEquals(HttpStatusCode.NotFound, response.status())
                        }
                    }
                }
            }
        }

        @Test
        fun `Test opplasting av ikke støttet vedleggformat`() {
            engine.handleRequestUploadImage(
                cookie = cookie,
                vedlegg = "jwkset.json".fromResources().readBytes(),
                contentType = "application/json",
                fileName = "jwkset.json",
                expectedCode = HttpStatusCode.BadRequest
            )
        }

        @Test
        fun `Test opplasting av for stort vedlegg`() {
            engine.handleRequestUploadImage(
                cookie = cookie,
                vedlegg = ByteArray(8 * 1024 * 1024 + 10),
                contentType = "image/png",
                fileName = "big_picture.png",
                expectedCode = HttpStatusCode.PayloadTooLarge
            )
        }
    }

    @Nested
    inner class OmsorgspengerUtvidetRettTest{
        @Test
        fun `Innsending av gyldig søknad`() {
            val søknad = Søknad(
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
            ).somJson()
            requestAndAssert(
                httpMethod = HttpMethod.Post,
                path = OMSORGSPENGER_UTVIDET_RETT_URL + INNSENDING_URL,
                expectedCode = HttpStatusCode.Accepted,
                jwtToken = tokenXToken,
                expectedResponse = null,
                requestEntity = søknad
            )
            hentOgAssertSøknad(søknad = JSONObject(søknad))
        }

        @Test
        fun `Innsending av ugyldig søknad som får valideringsfeil`() {
            val søknad = Søknad(
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
                    {
                      "type": "entity",
                      "name": "barn.norskIdentifikator",
                      "reason": "Ikke gyldig norskIdentifikator.",
                      "invalid_value": "123"
                    }, {
                      "name": "harBekreftetOpplysninger",
                      "reason": "Opplysningene må bekreftes for å sende inn søknad.",
                      "invalid_value": null,
                      "type": "entity"
                    }, {
                      "name": "harForståttRettigheterOgPlikter",
                      "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                      "invalid_value": null,
                      "type": "entity"
                    }
                  ],
                  "status": 400
                }
            """.trimIndent(),
                requestEntity = søknad
            )
        }
    }

    private fun requestAndAssert(
        httpMethod: HttpMethod,
        path: String,
        requestEntity: String? = null,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        jwtToken: String? = null,
        cookie: Cookie? = null
    ) : String? {
        val respons: String?
        with(engine) {
            handleRequest(httpMethod, path) {
                if (cookie != null) addHeader(HttpHeaders.Cookie, cookie.toString())
                if (jwtToken != null) addHeader(HttpHeaders.Authorization, "Bearer $jwtToken")
                logger.info("Request Entity = $requestEntity")
                addHeader(HttpHeaders.Accept, "application/json")
                if (requestEntity != null) addHeader(HttpHeaders.ContentType, "application/json")
                if (requestEntity != null) setBody(requestEntity)
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                respons = response.content
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
        return respons
    }

    private fun hentOgAssertSøknad(søknad: JSONObject){
        val hentet = kafkaKonsumer.hentOmsorgspengerUtvidetRettSøknad(søknad.getString("søknadId"))
        assertGyldigSøknad(søknad, hentet.data)
    }

    private fun assertGyldigSøknad(
        søknadSendtInn: JSONObject,
        søknadFraTopic: JSONObject
    ) {
        assertTrue(søknadFraTopic.has("søker"))
        assertTrue(søknadFraTopic.has("mottatt"))
        assertTrue(søknadFraTopic.has("k9FormatSøknad"))
        assertTrue(søknadFraTopic.getJSONObject("barn").has("norskIdentifikator"))

        assertEquals(søknadSendtInn.getString("søknadId"), søknadFraTopic.getString("søknadId"))
        assertEquals(søknadSendtInn.getString("relasjonTilBarnet"), søknadFraTopic.getString("relasjonTilBarnet"))

        assertEquals(
            søknadSendtInn.getJSONObject("barn").getString("navn"),
            søknadFraTopic.getJSONObject("barn").getString("navn")
        )
    }
}