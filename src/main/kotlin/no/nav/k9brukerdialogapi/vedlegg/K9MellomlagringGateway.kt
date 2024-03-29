package no.nav.k9brukerdialogapi.vedlegg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry.Companion.retry
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.ktor.metrics.Operation.Companion.monitored
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.k9MellomlagringKonfigurert
import no.nav.k9brukerdialogapi.utils.LoggingUtils.logTokenExchange
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.APPLICATION_JSON
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.TEXT_PLAIN
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Duration

class K9MellomlagringGateway(
    private val accessTokenClient: AccessTokenClient,
    private val k9MellomlagringScope: Set<String>,
    private val baseUrl: URI,
    private val exchangeTokenClient: CachedAccessTokenClient,
    private val k9MellomlagringTokenxAudience: Set<String>
) : HealthCheck {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9MellomlagringGateway::class.java)
        private val objectMapper = jacksonObjectMapper().k9MellomlagringKonfigurert()
        private const val SLETTE_VEDLEGG_OPERATION = "slette-vedlegg"
        private const val HENTE_VEDLEGG_OPERATION = "hente-vedlegg"
        private const val LAGRE_VEDLEGG_OPERATION = "lagre-vedlegg"
        private const val PERSISTER_VEDLEGG = "persister-vedlegg"
        private const val FJERNE_HOLD_PÅ_PERSISTERT_VEDLEGG = "fjerne-hold-på-persistert-vedlegg"
        private const val TJENESTE = "k9-brukerdialog-api"
    }

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)
    private val komplettUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "dokument")
    )

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(k9MellomlagringScope)
            Healthy("K9MellomlagringGateway", "Henting av access token for K9MellomlagringGateway.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for K9MellomlagringGateway", cause)
            UnHealthy("K9MellomlagringGateway", "Henting av access token for K9MellomlagringGateway.")
        }
    }

    suspend fun lagreVedlegg(
        vedlegg: Vedlegg,
        idToken: IdToken,
        callId: CallId
    ): String {
        val body = objectMapper.writeValueAsBytes(vedlegg)

        return retry(
            operation = LAGRE_VEDLEGG_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = monitored(
                app = TJENESTE,
                operation = LAGRE_VEDLEGG_OPERATION,
                resultResolver = { 201 == it.second.statusCode }
            ) {
                val contentStream = { ByteArrayInputStream(body) }
                val exchangeToken = IdToken(exchangeTokenClient.getAccessToken(k9MellomlagringTokenxAudience, idToken.value).token)
                logger.logTokenExchange(idToken, exchangeToken)
                komplettUrl
                    .toString()
                    .httpPost()
                    .body(contentStream)
                    .header(
                        HttpHeaders.Authorization to "Bearer ${exchangeToken.value}",
                        HttpHeaders.ContentType to APPLICATION_JSON,
                        HttpHeaders.Accept to APPLICATION_JSON,
                        HttpHeaders.XCorrelationId to callId.value
                    )
                    .awaitStringResponseResult()
            }
            result.fold(
                { success -> (objectMapper.readValue<CreatedResponseEntity>(success).id) },
                { error ->
                    logger.error(
                        "Error response = '${
                            error.response.body().asString(TEXT_PLAIN)
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved lagring av vedlegg.")
                })
        }
    }

    suspend fun slettVedlegg(
        vedleggId: String,
        idToken: IdToken,
        callId: CallId
    ): Boolean {
        val body = objectMapper.writeValueAsBytes(idToken.getNorskIdentifikasjonsnummer().somDokumentEier())

        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(vedleggId)
        )

        val exchangeToken = IdToken(exchangeTokenClient.getAccessToken(k9MellomlagringTokenxAudience, idToken.value).token)
        logger.logTokenExchange(idToken, exchangeToken)

        val httpRequest = urlMedId
            .toString()
            .httpDelete()
            .body(body)
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.value}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to APPLICATION_JSON
            )
        return requestSlettVedlegg(httpRequest)
    }

    private suspend fun requestSlettVedlegg(
        httpRequest: Request
    ): Boolean = retry(
        operation = SLETTE_VEDLEGG_OPERATION,
        initialDelay = Duration.ofMillis(200),
        factor = 2.0,
        logger = logger
    ) {
        val (request, _, result) = monitored(
            app = TJENESTE,
            operation = SLETTE_VEDLEGG_OPERATION,
            resultResolver = { 204 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        result.fold(
            { _ ->
                logger.info("Suksess ved sletting av vedlegg")
                true
            },
            { error ->
                logger.error("Error response = '${error.response.body().asString(TEXT_PLAIN)}' fra '${request.url}'")
                logger.error(error.toString())
                throw IllegalStateException("Feil ved sletting av vedlegg.")
            }
        )
    }

    private suspend fun requestHentVedlegg(
        httpRequest: Request
    ): Vedlegg? = retry(
        operation = HENTE_VEDLEGG_OPERATION,
        initialDelay = Duration.ofMillis(200),
        factor = 2.0,
        logger = logger
    ) {
        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = HENTE_VEDLEGG_OPERATION,
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        result.fold(
            { success ->
                logger.info("Suksess ved henting av vedlegg")
                ResolvedVedlegg(objectMapper.readValue<Vedlegg>(success))
            },
            { error ->
                if (404 == response.statusCode) ResolvedVedlegg()
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString(TEXT_PLAIN)
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av vedlegg.")
                }
            }
        ).vedlegg
    }

    internal suspend fun persisterVedlegg(
        vedleggId: List<String>,
        callId: CallId,
        eier: DokumentEier
    ) {
        val authorizationHeader: String = cachedAccessTokenClient.getAccessToken(k9MellomlagringScope).asAuthoriationHeader()

        coroutineScope {
            val deferred = mutableListOf<Deferred<Unit>>()
            vedleggId.forEach {
                deferred.add(async {
                    requestPersisterVedlegg(
                        vedleggId = it,
                        callId = callId,
                        eier = eier,
                        authorizationHeader = authorizationHeader
                    )
                })
            }
            deferred.awaitAll()
        }
    }


    private suspend fun requestPersisterVedlegg(
        vedleggId: String,
        callId: CallId,
        eier: DokumentEier,
        authorizationHeader: String
    ) {

        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(vedleggId, "persister")
        )

        val body = objectMapper.writeValueAsBytes(eier)

        val httpRequest = urlMedId.toString()
            .httpPut()
            .body(body)
            .header(
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to APPLICATION_JSON
            )

        val (request, _, result) = Operation.monitored(
            app = TJENESTE,
            operation = PERSISTER_VEDLEGG,
            resultResolver = { 204 == it.second.statusCode }
        ) {
            httpRequest.awaitStringResponseResult()
        }

        result.fold(
            { _ -> logger.info("Vellykket persistering av vedlegg") },
            { error ->
                logger.error("Error response = '${error.response.body().asString(TEXT_PLAIN)}' fra '${request.url}'")
                logger.error("Feil ved persistering av vedlegg. $error")
                throw IllegalStateException("Feil ved persistering av vedlegg.")
            }
        )
    }

    internal suspend fun fjernHoldPåPersistertVedlegg(
        vedleggId: List<String>,
        callId: CallId,
        eier: DokumentEier
    ) {
        val authorizationHeader: String =
            cachedAccessTokenClient.getAccessToken(k9MellomlagringScope).asAuthoriationHeader()

        coroutineScope {
            val deferred = mutableListOf<Deferred<Unit>>()
            vedleggId.forEach {
                deferred.add(async {
                    requestFjerneHoldPåPersisterVedlegg(
                        vedleggId = it,
                        callId = callId,
                        eier = eier,
                        authorizationHeader = authorizationHeader
                    )
                })
            }
            deferred.awaitAll()
        }
    }

    private suspend fun requestFjerneHoldPåPersisterVedlegg(
        vedleggId: String,
        callId: CallId,
        eier: DokumentEier,
        authorizationHeader: String
    ) {

        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf("persistert", vedleggId)
        )

        val body = objectMapper.writeValueAsBytes(eier)

        val httpRequest = urlMedId.toString()
            .httpPut()
            .body(body)
            .header(
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to APPLICATION_JSON
            )

        val (request, _, result) = Operation.monitored(
            app = TJENESTE,
            operation = FJERNE_HOLD_PÅ_PERSISTERT_VEDLEGG,
            resultResolver = { 200 == it.second.statusCode }
        ) {
            httpRequest.awaitStringResponseResult()
        }


        result.fold(
            { _ -> logger.info("Vellykket fjerning av hold på persistert vedlegg") },
            { error ->
                logger.error("Error response = '${error.response.body().asString(TEXT_PLAIN)}' fra '${request.url}'")
                logger.error("Feil ved fjerning av hold påpersistert vedlegg. $error")
            }
        )
    }

    suspend fun hentVedlegg(vedleggId: String, idToken: IdToken, callId: CallId): Vedlegg? {
        val body = objectMapper.writeValueAsBytes(idToken.getNorskIdentifikasjonsnummer().somDokumentEier())

        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(vedleggId)
        )

        val exchangeToken = IdToken(exchangeTokenClient.getAccessToken(k9MellomlagringTokenxAudience, idToken.value).token)
        logger.logTokenExchange(idToken, exchangeToken)

        val httpRequest = urlMedId
            .toString()
            .httpPost()
            .body(body)
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.value}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to APPLICATION_JSON,
                HttpHeaders.Accept to APPLICATION_JSON
            )
        return requestHentVedlegg(httpRequest)
    }

}

data class CreatedResponseEntity(val id: String)
private data class ResolvedVedlegg(val vedlegg: Vedlegg? = null)
