package no.nav.k9brukerdialogapi.wiremock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import no.nav.k9brukerdialogapi.k9MellomlagringKonfigurert
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.APPLICATION_JSON
import no.nav.k9brukerdialogapi.vedlegg.Vedlegg
import no.nav.k9brukerdialogapi.vedlegg.VedleggId
import java.util.*

class K9MellomlagringResponseTransformer() : ResponseTransformerV2 {

    val storage = mutableMapOf<VedleggId, Vedlegg>()
    val objectMapper = jacksonObjectMapper().k9MellomlagringKonfigurert()

    override fun getName(): String {
        return "K9MellomlagringResponseTransformer"
    }

    override fun transform(response: Response, event: ServeEvent): Response {
        val request = event.request
        return when {
            request == null -> throw IllegalStateException("request == null")
            request.erHealthCheck() -> Response.Builder.like(response).status(200).build()
            request.erHenteDokument() -> {

                val vedleggId = request.getVedleggId()
                return if (storage.containsKey(vedleggId)) {
                    Response.Builder.like(response)
                        .status(200)
                        .headers(
                            HttpHeaders(
                                HttpHeader.httpHeader("Content-Type", APPLICATION_JSON)
                            )
                        )
                        .body(objectMapper.writeValueAsString(storage[vedleggId]))
                        .build()
                } else {
                    Response.Builder.like(response)
                        .status(404)
                        .build()
                }
            }

            request.erLagreDokument() -> {
                val vedlegg = objectMapper.readValue<Vedlegg>(request.bodyAsString)
                val vedleggId = VedleggId(UUID.randomUUID().toString())
                storage[vedleggId] = vedlegg
                Response.Builder.like(response)
                    .status(201)
                    .headers(
                        HttpHeaders(
                            HttpHeader.httpHeader("Location", "http://localhost:8080/v1/dokument/${vedleggId.value}"),
                            HttpHeader.httpHeader("Content-Type", APPLICATION_JSON)
                        )
                    )
                    .body(
                        """
                        {
                            "id" : "${vedleggId.value}"
                        }
                    """.trimIndent()
                    )
                    .build()
            }

            request.method == RequestMethod.PUT -> {
                val vedleggId = VedleggId(UUID.randomUUID().toString())
                Response.Builder.like(response)
                    .status(201)
                    .headers(
                        HttpHeaders(
                            HttpHeader.httpHeader(
                                "Location",
                                "http://localhost:8080/v1/dokument/${vedleggId.value}/persister"
                            ),
                            HttpHeader.httpHeader("Content-Type", APPLICATION_JSON)
                        )
                    )
                    .body(
                        """
                        {
                            "id" : "${vedleggId.value}"

                        """.trimIndent()
                    )
                    .build()

            }

            request.method == RequestMethod.DELETE -> {
                val vedleggId = request.getVedleggId()
                if (storage.containsKey(vedleggId)) {
                    storage.remove(vedleggId)
                    Response.Builder.like(response)
                        .status(204)
                        .build()
                } else {
                    Response.Builder.like(response)
                        .status(404)
                        .build()
                }
            }
            else -> throw IllegalStateException("Uventet request.")
        }
    }

    override fun applyGlobally(): Boolean {
        return false
    }
}

private fun Request.getVedleggId(): VedleggId = VedleggId(url.substringAfterLast("/"))
private fun Request.erLagreDokument() = method == RequestMethod.POST && url.substringAfterLast("/") == "dokument"
private fun Request.erHenteDokument() = method == RequestMethod.POST && url.substringAfterLast("/") != "dokument"
private fun Request.erHealthCheck() = method == RequestMethod.GET && url.substringAfterLast("/") == "health"
