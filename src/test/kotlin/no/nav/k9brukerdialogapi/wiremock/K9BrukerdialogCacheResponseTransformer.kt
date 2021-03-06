package no.nav.k9brukerdialogapi.wiremock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import com.github.tomakehurst.wiremock.http.HttpHeaders
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.k9BrukerdialogCacheKonfigurert
import no.nav.k9brukerdialogapi.mellomlagring.CacheRequest
import no.nav.k9brukerdialogapi.somJson
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class K9BrukerdialogCacheResponseTransformer() : ResponseTransformer() {

    internal companion object {
        private val logger = LoggerFactory.getLogger(K9BrukerdialogCacheResponseTransformer::class.java)
        val mellomlagredeVerdierCache = mutableMapOf<String, Cache>()
        val objectMapper = jacksonObjectMapper().k9BrukerdialogCacheKonfigurert()
    }

    override fun getName(): String {
        return "K9BrukerdialogCacheResponseTransformer"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        return when {
            request == null -> throw IllegalStateException("request == null")
            request.erHealthCheck() -> Response.Builder.like(response).status(200).build()

            request.erLagreCache() -> {
                val cacheRequest = objectMapper.readValue<CacheRequest>(request.bodyAsString)
                val fnr = request.fnr()
                val nøkkel = "${cacheRequest.nøkkelPrefiks}_$fnr"
                val cache = cacheRequest.somCache(nøkkel)

                logger.info("Lagrer i cache... {}", cache)
                return if (mellomlagredeVerdierCache.containsKey(nøkkel)) {
                    logger.warn("Cache med nøkkel: {} eksisterer allerede.", nøkkel)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.Conflict.value)
                        .build()
                } else {
                    mellomlagredeVerdierCache[nøkkel] = cache
                    logger.info("Lagret i cache: {}", cache)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.Created.value)
                        .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                        .body(cache.somJson())
                        .build()
                }
            }

            request.erHenteCache() -> {
                val nøkkelPrefiks = request.url.substringAfterLast("/")
                val nøkkel = "${nøkkelPrefiks}_${request.fnr()}"
                logger.info("Henter fra cache med nøkkel... {}", nøkkel)

                return if (mellomlagredeVerdierCache.containsKey(nøkkel)) {
                    val cache = mellomlagredeVerdierCache[nøkkel]!!.somJson()
                    logger.info("Cache hentet: {}", cache)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.OK.value)
                        .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                        .body(cache)
                        .build()
                } else {
                    logger.warn("Fant ikke cache med nøkkel: {}", nøkkel)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.NotFound.value)
                        .build()
                }
            }

            request.erOppdatereCache() -> {
                val cacheRequest = objectMapper.readValue<CacheRequest>(request.bodyAsString)
                val nøkkelPrefiks = request.url.substringAfterLast("/")
                val nøkkel = "${nøkkelPrefiks}_${request.fnr()}"
                val cache = cacheRequest.somCache(nøkkel)

                logger.info("Oppdaterer cache med... {}", cache)
                if (mellomlagredeVerdierCache.containsKey(nøkkel)) {
                    mellomlagredeVerdierCache[nøkkel] = cache
                    logger.info("Cache oppdatert med: {}", cache)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.OK.value)
                        .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                        .body(cache.somJson())
                        .build()
                } else {
                    logger.warn("Fant ikke cache med nøkkel: {}", nøkkel)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.NotFound.value)
                        .build()
                }
            }

            request.erSletteCache() -> {
                val nøkkelPrefiks = request.url.substringAfterLast("/")
                val nøkkel = "${nøkkelPrefiks}_${request.fnr()}"
                logger.info("Sletter cache med nøkkel {}...", nøkkel)
                if (mellomlagredeVerdierCache.containsKey(nøkkel)) {
                    mellomlagredeVerdierCache.remove(nøkkel)
                    logger.info("Cache med nøkkel {} slettet.", nøkkel)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.NoContent.value)
                        .build()
                } else {
                    logger.warn("Fant ikke cache med nøkkel: {}", nøkkel)
                    Response.Builder.like(response)
                        .status(HttpStatusCode.NotFound.value)
                        .build()
                }
            }
            else -> throw IllegalStateException("Uventet request.")
        }
    }

    data class Cache(
        val nøkkel: String,
        val verdi: String,
        val utløpsdato: ZonedDateTime,
        val opprettet: ZonedDateTime? = null,
        val endret: ZonedDateTime? = null
    )

    private fun CacheRequest.somCache(nøkkel: String) = Cache(
        nøkkel = nøkkel,
        verdi = verdi,
        utløpsdato = utløpsdato,
        opprettet = opprettet,
        endret = endret
    )

    private fun Request.fnr(): String {
        val authHeader = getHeader(Authorization)
        return IdToken(authHeader.substringAfterLast("Bearer ")).getNorskIdentifikasjonsnummer()
    }

    private fun Request.erLagreCache() = method == RequestMethod.POST
    private fun Request.erHenteCache() = method == RequestMethod.GET
    private fun Request.erOppdatereCache() = method == RequestMethod.PUT
    private fun Request.erSletteCache() = method == RequestMethod.DELETE
    private fun Request.erHealthCheck() = method == RequestMethod.GET && url.substringAfterLast("/") == "health"
}