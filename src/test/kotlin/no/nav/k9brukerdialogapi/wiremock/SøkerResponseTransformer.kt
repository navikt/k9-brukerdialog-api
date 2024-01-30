package no.nav.k9brukerdialogapi.wiremock

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import no.nav.k9brukerdialogapi.TestUtils

class SokerResponseTransformer : ResponseTransformerV2 {
    override fun getName(): String {
        return "k9-oppslag-soker"
    }

    override fun transform(response: Response, event: ServeEvent): Response {
        return Response.Builder.like(response)
            .body(
                getResponse(
                    ident = TestUtils.getIdentFromIdToken(event.request)
                )
            )
            .build()
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(ident: String): String {
    when(ident) {
        "25037139184" -> {
            return """
        { 
            "aktør_id": "23456",
            "fornavn": "ARNE",
            "mellomnavn": "BJARNE",
            "etternavn": "CARLSEN",
            "fødselsdato": "1990-01-02"
        }
        """.trimIndent()
        } "290990123456" -> {
            return """
            {
                "etternavn": "MORSEN",
                "fornavn": "MOR",
                "mellomnavn": "HEISANN",
                "aktør_id": "12345",
                "fødselsdato": "1997-05-25"
            }
        """.trimIndent()
        } "12125012345" -> {
            return """
            {
                "etternavn": "MORSEN",
                "fornavn": "MOR",
                "mellomnavn": "HEISANN",
                "aktør_id": "12345",
                "fødselsdato": "2050-12-12"
            }
        """.trimIndent()
        } "02119970078" -> {
        return """
            {
                "etternavn": "MORSEN",
                "fornavn": "MOR",
                "mellomnavn": "HEISANN",
                "aktør_id": "12345",
                "fødselsdato": "1999-11-02"
            }
        """.trimIndent()
        } else -> {
            return """
                {}
            """.trimIndent()
        }
    }
}
