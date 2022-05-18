package no.nav.helse

import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TestUtils {
    companion object {

        internal fun MutableList<String>.validerFeil(antallFeil: Int, valideringsfeil: List<String> = listOf()) {
            Assert.assertEquals(antallFeil, this.size)

            if(valideringsfeil.isNotEmpty()){
                valideringsfeil.forEach {
                    assertContains(this, it)
                }
            }
        }

        internal fun MutableList<String>.validerIngenFeil() {
            Assert.assertTrue(this.isEmpty())
        }

        fun getIdentFromIdToken(request: Request?): String {
            val idToken = IdToken(request!!.getHeader(HttpHeaders.Authorization).substringAfter("Bearer "))
            return idToken.getNorskIdentifikasjonsnummer()
        }

        fun MockOAuth2Server.issueToken(
            fnr: String,
            issuerId: String = "tokendings",
            audience: String = "dev-gcp:dusseldorf:k9-brukerdialog-api",
            claims: Map<String, String> = mapOf("acr" to "Level4"),
            cookieName: String = "selvbetjening-idtoken",
            somCookie: Boolean = false,
        ): String {
            val jwtToken =
                issueToken(issuerId = issuerId, subject = fnr, audience = audience, claims = claims).serialize()
            return when (somCookie) {
                false -> jwtToken
                true -> "$cookieName=$jwtToken"
            }
        }

        fun requestAndAssert(
            httpMethod: HttpMethod,
            path: String,
            requestEntity: String? = null,
            expectedResponse: String? = null,
            expectedCode: HttpStatusCode,
            jwtToken: String? = null,
            cookie: String? = null,
            logger: Logger,
            engine: TestApplicationEngine
        ): String? {
            val respons: String?
            with(engine) {
                handleRequest(httpMethod, path) {
                    if (cookie != null) addHeader(HttpHeaders.Cookie, cookie)
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
    }
}
