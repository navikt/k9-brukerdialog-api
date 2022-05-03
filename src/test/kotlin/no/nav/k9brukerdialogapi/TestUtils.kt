package no.nav.helse

import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
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
            jwtToken: String,
            cookieName: String = "selvbetjening-idtoken"
        ): String {
            return "$cookieName=$jwtToken"
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
