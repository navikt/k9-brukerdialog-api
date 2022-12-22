package no.nav.k9brukerdialogapi.mellomlagring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.metrics.Operation.Companion.monitored
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.k9BrukerdialogCacheKonfigurert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

class K9BrukerdialogCacheGateway(
    private val tokenxClient: CachedAccessTokenClient,
    private val k9BrukerdialogCacheTokenxAudience: Set<String>,
    baseUrl: URI
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9BrukerdialogCacheGateway::class.java)
        private val objectMapper = jacksonObjectMapper().k9BrukerdialogCacheKonfigurert()
        private const val LAGRE_CACHE_OPERATION = "lagre-cache"
        private const val HENTE_CACHE_OPERATION = "hente-cache"
        private const val OPPDATERE_CACHE_OPERATION = "oppdatere-cache"
        private const val SLETTE_CACHE_OPERATION = "slette-cache"
        private const val TJENESTE = "k9-brukerdialog-api"
    }

    private val komplettUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api", "cache")
    )

    suspend fun mellomlagreSøknad(
        cacheRequest: CacheRequest,
        idToken: IdToken,
        callId: CallId
    ): CacheResponse {
        val body = objectMapper.writeValueAsBytes(cacheRequest)

        val exchangeToken = tokenxClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = komplettUrl
            .toString()
            .httpPost()
            .body(body)
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = LAGRE_CACHE_OPERATION,
            resultResolver = { HttpStatusCode.Created.value == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved mellomlagring av søknad")
                objectMapper.readValue<CacheResponse>(success)
            },
            { error ->
                if (HttpStatusCode.Conflict.value == response.statusCode) throw CacheConflictException(cacheRequest.nøkkelPrefiks)
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved mellomlagring av søknad.")
                }
            }
        )
    }

    suspend fun hentMellomlagretSøknad(nøkkelPrefiks: String, idToken: IdToken, callId: CallId): CacheResponse? {
        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(nøkkelPrefiks)
        )

        val exchangeToken = tokenxClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = urlMedId
            .toString()
            .httpGet()
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = HENTE_CACHE_OPERATION,
            resultResolver = { HttpStatusCode.OK.value == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved henting av mellomlagret søknad")
                objectMapper.readValue<CacheResponse>(success)
            },
            { error ->
                if (HttpStatusCode.NotFound.value == response.statusCode) null
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av mellomlagret søknad.")
                }
            }
        )
    }


    suspend fun oppdaterMellomlagretSøknad(
        cacheRequest: CacheRequest,
        idToken: IdToken,
        callId: CallId
    ): CacheResponse {
        val body = objectMapper.writeValueAsBytes(cacheRequest)

        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(cacheRequest.nøkkelPrefiks)
        )

        val exchangeToken = tokenxClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = urlMedId
            .toString()
            .httpPut()
            .body(body)
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = OPPDATERE_CACHE_OPERATION,
            resultResolver = { HttpStatusCode.OK.value == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved oppdatering av mellomlagret søknad")
                objectMapper.readValue<CacheResponse>(success)
            },
            { error ->
                if (response.statusCode == HttpStatusCode.NotFound.value) throw CacheNotFoundException(cacheRequest.nøkkelPrefiks)
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved oppdatering av mellomlagret søknad.")
                }
            }
        )
    }

    suspend fun slettMellomlagretSøknad(nøkkelPrefiks: String, idToken: IdToken, callId: CallId): Boolean {
        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(nøkkelPrefiks)
        )

        val exchangeToken = tokenxClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = urlMedId
            .toString()
            .httpDelete()
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = SLETTE_CACHE_OPERATION,
            resultResolver = { HttpStatusCode.NoContent.value == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved sletting av mellomlagret søknad")
                true
            },
            { error ->
                if (HttpStatusCode.NotFound.value == response.statusCode) true
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved sletting av mellomlagret søknad.")
                }
            }
        )
    }
}
