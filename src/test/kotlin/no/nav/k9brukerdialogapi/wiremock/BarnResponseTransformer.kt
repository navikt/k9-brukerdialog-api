package no.nav.k9brukerdialogapi.wiremock

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import no.nav.k9brukerdialogapi.TestUtils

class BarnResponseTransformer : ResponseTransformerV2 {
    override fun getName(): String {
        return "k9-oppslag-barn"
    }

    override fun transform(response: Response, event: ServeEvent): Response {
        return Response.Builder.like(response)
            .body(getResponse(
                ident = TestUtils.getIdentFromIdToken(event.request)
            ))
            .build()
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(ident: String): String {
    when(ident) {
        "02119970078" -> {
            return """
            {
                "barn": [{
                    "fødselsdato": "2000-08-27",
                    "fornavn": "BARN",
                    "mellomnavn": "EN",
                    "etternavn": "BARNESEN",
                    "aktør_id": "1000000000001",
                    "har_samme_adresse": true,
                    "identitetsnummer": "18909798651"
                }, {
                    "fødselsdato": "2001-04-10",
                    "fornavn": "BARN",
                    "mellomnavn": "TO",
                    "etternavn": "BARNESEN",
                    "aktør_id": "1000000000002",
                    "har_samme_adresse": true,
                    "identitetsnummer": "10910198357"
                }]
            }
            """.trimIndent()
        } else -> {
            return """
                {
                    "barn": []
                }
            """.trimIndent()
        }
    }
}
