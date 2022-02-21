package no.nav.k9brukerdialogapi.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.helse.TestUtils
import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.frilansoppdragQueryName
import no.nav.k9brukerdialogapi.somJson
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory

const val orgQueryName = "org"

class ArbeidsgivereResponseTransformer : ResponseTransformer() {
    private companion object {
        private val logger = LoggerFactory.getLogger(ArbeidsgivereResponseTransformer::class.java)
    }

    override fun transform(
        request: Request,
        response: Response,
        files: FileSource?,
        parameters: Parameters?
    ): Response {

        val orgnummere = try {
            val values = request.queryParameter(orgQueryName).values()
            logger.info("Etterspurt organisasjonsnummer: {}", values)
            values
        } catch (ex: Exception) {
            null
        }
        val skalHenteFrilansoppdrag = request.queryParameter("a").containsValue("frilansoppdrag[]")
        val skalHentePrivateArbeidsgivere = request.queryParameter("a").containsValue("private_arbeidsgivere[].offentlig_ident")

        return Response.Builder.like(response)
            .body(
                getResponse(
                    ident = TestUtils.getIdentFromIdToken(request),
                    frilansoppdrag = skalHenteFrilansoppdrag,
                    privateArbeidsgivere = skalHentePrivateArbeidsgivere,
                    organisasjonsnummere = orgnummere
                )
            )
            .build()
    }

    override fun getName(): String {
        return "k9-oppslag-arbeidsgivere"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(
    ident: String,
    frilansoppdrag: Boolean,
    privateArbeidsgivere: Boolean,
    organisasjonsnummere: List<String>?
): String {
    val json = JSONObject()
    val arbeidsgivereJson = JSONObject()
    val organisasjon1 = JSONObject().apply {
        put("navn", "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ")
        put("organisasjonsnummer", "913548221")
    }
    val organisasjon2 = JSONObject().apply {
        put("navn", "NAV, AVD WALDEMAR THRANES GATE")
        put("organisasjonsnummer", "984054564")
    }
    val privatArbeidsgiver1 = JSONObject().apply {
        put("offentlig_ident", "10047206508")
        put("ansatt_fom", "2014-07-01")
        put("ansatt_tom", "2015-12-31")
    }
    val frilansoppdrag1 = JSONObject().apply {
        put("type", "Person")
        put("ansatt_fom", "2020-01-01")
        put("ansatt_tom", "2022-02-28")
        put("offentlig_ident", "805824352")
    }
    val frilansoppdrag2 = JSONObject().apply {
        put("type", "Organisasjon")
        put("ansatt_fom", "2020-01-01")
        put("ansatt_tom", "2022-02-28")
        put("organisasjonsnummer", "123456789")
        put("navn", "DNB, FORSIKRING")
    }

    when {
        organisasjonsnummere.isNullOrEmpty() -> when (ident) {
            "02119970078" -> {
                val organisasjonerJson = JSONArray().apply {
                    put(organisasjon1)
                    put(organisasjon2)
                }
                arbeidsgivereJson.put("organisasjoner", organisasjonerJson)

                if (privateArbeidsgivere) {
                    val privatArbeidsgiverJson = JSONArray().apply {
                        put(privatArbeidsgiver1)
                    }
                    arbeidsgivereJson.put("private_arbeidsgivere", privatArbeidsgiverJson)
                }

                if (frilansoppdrag) {
                    val frilansoppdragJson = JSONArray().apply {
                        put(frilansoppdrag1)
                        put(frilansoppdrag2)
                    }
                    arbeidsgivereJson.put("frilansoppdrag", frilansoppdragJson)
                }

                json.put("arbeidsgivere", arbeidsgivereJson)
                return json.toString()
            }
            "19116812889" -> {
                val organisasjonerJson = JSONArray().apply {
                    put(organisasjon1)
                    put(organisasjon2)
                }
                arbeidsgivereJson.put("organisasjoner", organisasjonerJson)
                json.put("arbeidsgivere", arbeidsgivereJson)
                return json.toString()
            }
            else -> {
                arbeidsgivereJson.put("organisasjoner", JSONArray())
                json.put("arbeidsgivere", arbeidsgivereJson)
                return json.toString()
            }
        }
        else ->
            //language=json
            return """
                {
                    "arbeidsgivere": {
                        "organisasjoner": ${organisasjonsnummere.map { org(it) }}
                    }
                }
            """.trimIndent()
    }
}

private fun org(org: String) = when (org) {
    //language=json
    "977302390" -> """
          {
            "navn": "INMETA CONSULTING AS",
            "organisasjonsnummer": "977302390"
          }""".trimIndent()

    //language=json
    "984054564" -> """
        {
            "navn": "NAV, AVD WALDEMAR THRANES GATE",
            "organisasjonsnummer": "984054564"
        }
    """.trimIndent()

    //language=json
    "995784637" -> """
        {
            "navn": null,
            "organisasjonsnummer": "995784637"
        }
    """.trimIndent()

    else -> ""
}

