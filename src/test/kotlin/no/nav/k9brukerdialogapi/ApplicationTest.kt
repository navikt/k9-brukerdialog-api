package no.nav.k9brukerdialogapi

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.prometheus.client.CollectorRegistry
import no.nav.helse.TestUtils.Companion.issueToken
import no.nav.helse.TestUtils.Companion.requestAndAssert
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.wiremock.*
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
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
        val mockOAuth2Server = MockOAuth2Server().apply { start() }
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
            .stubK9BrukerdialogCache()

        private val kafkaEnvironment = KafkaWrapper.bootstrap()

        private val gyldigFødselsnummerA = "02119970078"
        private val gyldigFodselsnummerB = "02119970079"
        private const val ikkeMyndigFnr = "12125012345"

        private val cookie = mockOAuth2Server.issueToken(
            issuerId = "login-service",
            fnr = gyldigFødselsnummerA,
            somCookie = true
        )

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
            logger.info("Tearing down")
            wireMockServer.stop()
            kafkaEnvironment.tearDown()
            mockOAuth2Server.shutdown()
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
    inner class SøkerOppslagTest {

        @Test
        fun `Hente søker med loginservice token som cookie`() {
            requestAndAssert(
                engine = engine,
                logger = logger,
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
                cookie = cookie
            )
        }

        @Test
        fun `Hente søker med tokenX token som authorization header`() {
            requestAndAssert(
                engine = engine,
                logger = logger,
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
                engine = engine,
                logger = logger,
                httpMethod = HttpMethod.Get,
                path = OPPSLAG_URL + SØKER_URL,
                expectedCode = HttpStatusCode.fromValue(451),
                cookie = mockOAuth2Server.issueToken(issuerId = "login-service", fnr = ikkeMyndigFnr, somCookie = true),
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
            )

            wireMockServer.stubK9OppslagSoker() // reset til default mapping

            @Test
            fun `Hente søker med tilgangsnivå 3`() {
                requestAndAssert(
                    engine = engine,
                    logger = logger,
                    httpMethod = HttpMethod.Get,
                    path = OPPSLAG_URL + SØKER_URL,
                    cookie = mockOAuth2Server.issueToken(
                        issuerId = "login-service",
                        fnr = gyldigFødselsnummerA,
                        claims = mapOf("acr" to "Level3"),
                        somCookie = true
                    ),
                    expectedCode = HttpStatusCode.Forbidden,
                    expectedResponse = null
                )
            }
        }

        @Nested
        inner class MellomlagringTest {

            @Test
            fun `Sende inn, hente, oppdatere og slette mellomlagring`() {
                val mellomlagringSøknad = """
                {
                    "mellomlagring": "soknad"
                }
            """.trimIndent()

                requestAndAssert(
                    httpMethod = HttpMethod.Post,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = tokenXToken,
                    expectedCode = HttpStatusCode.Created,
                    expectedResponse = null,
                    requestEntity = mellomlagringSøknad,
                    engine = engine,
                    logger = logger
                )

                requestAndAssert(
                    httpMethod = HttpMethod.Get,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = tokenXToken,
                    expectedCode = HttpStatusCode.OK,
                    expectedResponse = mellomlagringSøknad,
                    engine = engine,
                    logger = logger
                )
                val oppdatertMellomlagringSøknad = """
                {
                    "mellomlagring": "oppdatert soknad"
                }
            """.trimIndent()

                requestAndAssert(
                    httpMethod = HttpMethod.Put,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = tokenXToken,
                    expectedCode = HttpStatusCode.NoContent,
                    requestEntity = oppdatertMellomlagringSøknad,
                    engine = engine,
                    logger = logger
                )

                requestAndAssert(
                    httpMethod = HttpMethod.Get,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = tokenXToken,
                    expectedCode = HttpStatusCode.OK,
                    expectedResponse = oppdatertMellomlagringSøknad,
                    engine = engine,
                    logger = logger
                )

                requestAndAssert(
                    httpMethod = HttpMethod.Delete,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = tokenXToken,
                    expectedCode = HttpStatusCode.Accepted,
                    engine = engine,
                    logger = logger
                )

                requestAndAssert(
                    httpMethod = HttpMethod.Get,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = tokenXToken,
                    expectedCode = HttpStatusCode.OK,
                    expectedResponse = """{}""".trimIndent(),
                    engine = engine,
                    logger = logger
                )

            }

            @Test
            fun `gitt mellomlagring ikke eksisterer, forvent tomt objekt`() {

                requestAndAssert(
                    httpMethod = HttpMethod.Get,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = mockOAuth2Server.issueToken(
                        issuerId = "tokendings",
                        subject = gyldigFodselsnummerB,
                        audience = "dev-gcp:dusseldorf:k9-brukerdialog-api",
                        claims = mapOf("acr" to "Level4")
                    ).serialize(),
                    expectedCode = HttpStatusCode.OK,
                    expectedResponse = """{}""",
                    engine = engine,
                    logger = logger
                )
            }

            @Test
            fun `gitt det mellomlagres på en eksisterende nøkkel, forvent konfliktfeil`() {
                val mellomlagringSøknad = """
                {
                    "mellomlagring": "soknad"
                }
            """.trimIndent()

                requestAndAssert(
                    httpMethod = HttpMethod.Post,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = mockOAuth2Server.issueToken(
                        issuerId = "tokendings",
                        subject = "02119970080",
                        audience = "dev-gcp:dusseldorf:k9-brukerdialog-api",
                        claims = mapOf("acr" to "Level4")
                    ).serialize(),
                    expectedCode = HttpStatusCode.Created,
                    expectedResponse = null,
                    requestEntity = mellomlagringSøknad,
                    engine = engine,
                    logger = logger
                )

                requestAndAssert(
                    httpMethod = HttpMethod.Post,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = mockOAuth2Server.issueToken(
                        issuerId = "tokendings",
                        subject = "02119970080",
                        audience = "dev-gcp:dusseldorf:k9-brukerdialog-api",
                        claims = mapOf("acr" to "Level4")
                    ).serialize(),
                    expectedCode = HttpStatusCode.Conflict,
                    requestEntity = mellomlagringSøknad,
                    engine = engine,
                    logger = logger,
                    expectedResponse = """
                {
                  "type": "/problem-details/cache-conflict",
                  "title": "cache-conflict",
                  "status": 409,
                  "detail": "Konflikt ved mellomlagring. Nøkkel eksisterer allerede.",
                  "instance": "mellomlagring/OMSORGSDAGER_ALENEOMSORG"
                }
            """.trimIndent()
                )
            }

            @Test
            fun `gitt sletting av en ikke-eksisterende nøkkel, forvent ingen feil`() {
                val mellomlagringSøknad = """
                {
                    "mellomlagring": "soknad"
                }
            """.trimIndent()

                requestAndAssert(
                    httpMethod = HttpMethod.Delete,
                    path = "mellomlagring/OMSORGSDAGER_ALENEOMSORG",
                    jwtToken = mockOAuth2Server.issueToken(
                        issuerId = "tokendings",
                        subject = "02119970081",
                        audience = "dev-gcp:dusseldorf:k9-brukerdialog-api",
                        claims = mapOf("acr" to "Level4")
                    ).serialize(),
                    expectedCode = HttpStatusCode.Accepted,
                    requestEntity = mellomlagringSøknad,
                    expectedResponse = null,
                    engine = engine,
                    logger = logger,
                )
            }
        }

        @Nested
        inner class BarnOppslagTest {
            @Test
            fun `Hente barn og eksplisit sjekke at identitetsnummer ikke blir med ved get kall`() {
                val respons = requestAndAssert(
                    engine = engine,
                    logger = logger,
                    httpMethod = HttpMethod.Get,
                    path = OPPSLAG_URL + BARN_URL,
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
                    engine = engine,
                    logger = logger,
                    httpMethod = HttpMethod.Get,
                    path = OPPSLAG_URL + BARN_URL,
                    expectedCode = HttpStatusCode.OK,
                    expectedResponse = """
                        {
                            "barn": []
                        }
                        """.trimIndent(),
                    cookie = mockOAuth2Server.issueToken(
                        issuerId = "login-service",
                        fnr = "26104500284",
                        somCookie = true
                    )
                )
                wireMockServer.stubK9OppslagBarn()
            }
        }

        @Nested
        inner class ArbeidsgiverOppslagTest {
            @Test
            fun `Oppslag av alle arbeidsgiver inkludert private og frilansoppdrag`() {
                requestAndAssert(
                    engine = engine,
                    logger = logger,
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
            fun `Oppslag av arbeidsgivere uten private og frilansoppdrag`() {
                requestAndAssert(
                    engine = engine,
                    logger = logger,
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
        inner class VedleggTest {
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
    }
}
