package no.nav.helse

import com.github.tomakehurst.wiremock.http.Cookie
import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.testsupport.jws.IDPorten
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.helse.dusseldorf.testsupport.jws.Tokendings
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import kotlin.test.assertEquals

class TestUtils {
    companion object {

        fun getIdentFromIdToken(request: Request?): String {
            val idToken = IdToken(request!!.getHeader(HttpHeaders.Authorization).substringAfter("Bearer "))
            return idToken.getNorskIdentifikasjonsnummer()
        }

        fun getAuthCookie(
            fnr: String,
            level: Int = 4,
            cookieName: String = "localhost-idtoken",
            expiry: Long? = null) : Cookie {

            val overridingClaims : Map<String, Any> = if (expiry == null) emptyMap() else mapOf(
                "exp" to expiry
            )

            val jwt = LoginService.V1_0.generateJwt(fnr = fnr, level = level, overridingClaims = overridingClaims)
            return Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))
        }

        fun getTokenDingsToken(
            fnr: String,
            level: Int = 4,
            expiry: Long? = null
        ): String {

            val overridingClaims: Map<String, Any> = if (expiry == null) emptyMap() else mapOf(
                "exp" to expiry,
                "acr" to "Level4"
            )

            return Tokendings.generateJwt(
                overridingClaims = overridingClaims,
                urlDecodedBody = Tokendings.generateUrlDecodedBody(
                    grantType = "urn:ietf:params:oauth:grant-type:token-exchange",
                    clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                    clientId = "dev-gcp:dusseldorf:k9-ettersending",
                    clientAssertion = Tokendings.generateAssertionJwt(
                        mapOf(
                            "iss" to Tokendings.getIssuer(),
                            "client_id" to "dev-gcp:dusseldorf:k9-ettersending",
                            "sub" to "dev-gcp:dusseldorf:k9-ettersending",
                            "aud" to Tokendings.getAudience()
                        )
                    ),
                    subjectTokenType = "urn:ietf:params:oauth:token-type:jwt",
                    subjectToken = IDPorten.generateIdToken(fnr = fnr, level = level, overridingClaims = overridingClaims)
                )
            )
        }

        fun requestAndAssert(
            httpMethod: HttpMethod,
            path: String,
            requestEntity: String? = null,
            expectedResponse: String? = null,
            expectedCode: HttpStatusCode,
            jwtToken: String? = null,
            cookie: Cookie? = null,
            logger: Logger,
            engine: TestApplicationEngine
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
    }
}
