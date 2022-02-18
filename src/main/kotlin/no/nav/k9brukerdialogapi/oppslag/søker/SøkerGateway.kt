package no.nav.k9brukerdialogapi.oppslag.søker

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.k9SelvbetjeningOppslagKonfigurert
import no.nav.k9brukerdialogapi.oppslag.genererOppslagHttpRequest
import no.nav.k9brukerdialogapi.oppslag.throwable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration

class SøkerGateway(
    private val baseUrl: URI,
    private val accessTokenClient: CachedAccessTokenClient,
    private val k9SelvbetjeningOppslagTokenxAudience: Set<String>,
) {
    private val logger: Logger = LoggerFactory.getLogger(SøkerGateway::class.java)
    private val HENTE_SOKER_OPERATION = "hente-soker"
    private val objectMapper = jacksonObjectMapper().k9SelvbetjeningOppslagKonfigurert()
    private val attributter = Pair("a", listOf("aktør_id", "fornavn", "mellomnavn", "etternavn", "fødselsdato"))

    suspend fun hentSøker(
        idToken: IdToken,
        callId: CallId
    ): Søker {
        val exchangeToken = IdToken(accessTokenClient.getAccessToken(k9SelvbetjeningOppslagTokenxAudience, idToken.value).token)
        logger.info("Utvekslet token fra {} med token fra {}.", idToken.issuer(), exchangeToken.issuer())

        val httpRequest = genererOppslagHttpRequest(
            baseUrl = baseUrl,
            attributter = attributter,
            idToken = idToken,
            callId = callId
        )

        val oppslagRespons = Retry.retry(
            operation = HENTE_SOKER_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "k9-brukerdialog-api",
                operation = HENTE_SOKER_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<SøkerOppslagRespons>(success) },
                { error ->
                    throw error.throwable(
                        request = request,
                        logger = logger,
                        errorMessage = "Feil ved henting av søkers personinformasjon"
                    )
                }
            )
        }
        return oppslagRespons.tilSøker(idToken.getNorskIdentifikasjonsnummer())
    }
}