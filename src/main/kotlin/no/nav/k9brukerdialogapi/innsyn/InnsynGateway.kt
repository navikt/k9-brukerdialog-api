package no.nav.k9brukerdialogapi.innsyn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.vedlegg.K9MellomlagringGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class InnsynGateway(
    val baseUrl: URI,
    private val exchangeTokenClient: CachedAccessTokenClient,
    private val sifInnsynAudience: Set<String>
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(InnsynGateway::class.java)
        private const val HENTE_SOKNAD_OPPLYSNINGER_OPERATION = "hente-soknad-opplysninger"
        private val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
    }

    suspend fun hentSøknadsopplysninger(
        idToken: IdToken,
        callId: CallId
    ): List<K9SakInnsynSøknad> {
        val innsynSakUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("innsyn", "sak")
        ).toString()
        val httpRequest = generateHttpRequest(idToken, innsynSakUrl, callId)

        val oppslagRespons = Retry.retry(
            operation = HENTE_SOKNAD_OPPLYSNINGER_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_SOKNAD_OPPLYSNINGER_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<List<K9SakInnsynSøknad>>(success) },
                { error ->
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av søknadsopplysninger")
                }
            )
        }
        return oppslagRespons
    }

    private fun generateHttpRequest(
        idToken: IdToken,
        url: String,
        callId: CallId
    ): Request {
        val exchangeToken = IdToken(exchangeTokenClient.getAccessToken(sifInnsynAudience, idToken.value).token)
        logger.info("Utvekslet token fra {} med token fra {}.", idToken.issuer(), exchangeToken.issuer())
        return url
            .httpGet()
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.value}",
                HttpHeaders.Accept to "application/json",
                HttpHeaders.XCorrelationId to callId.value
            )
    }
}

data class InnsynBarn(
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktørId: String,
    val identitetsnummer: String? = null
)

data class K9SakInnsynSøknad(
    val barn: InnsynBarn,
    val søknad: Søknad
)


