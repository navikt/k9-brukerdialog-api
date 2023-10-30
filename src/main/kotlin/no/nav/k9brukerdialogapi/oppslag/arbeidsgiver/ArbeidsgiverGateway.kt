package no.nav.k9brukerdialogapi.oppslag.arbeidsgiver

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
import no.nav.k9brukerdialogapi.utils.LoggingUtils.logTokenExchange
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration

class ArbeidsgiverGateway(
    private val baseUrl: URI,
    private val accessTokenClient: CachedAccessTokenClient,
    private val k9SelvbetjeningOppslagTokenxAudience: Set<String>,
) {

    private val logger: Logger = LoggerFactory.getLogger("nav.ArbeidsgivereGateway")
    private val HENTE_ARBEIDSGIVERE_OPERATION = "hente-arbeidsgivere"
    private val objectMapper = jacksonObjectMapper().k9SelvbetjeningOppslagKonfigurert()

    internal suspend fun hentArbeidsgivere(
        idToken: IdToken,
        callId: CallId,
        attributter: List<Pair<String, List<String>>>,
        ytelse: Ytelse
    ): Arbeidsgivere {
        val exchangeToken = IdToken(accessTokenClient.getAccessToken(k9SelvbetjeningOppslagTokenxAudience, idToken.value).token)
        logger.logTokenExchange(idToken, exchangeToken)

        val httpRequest = genererOppslagHttpRequest(
            pathParts = "meg", baseUrl = baseUrl, attributter = attributter, idToken = exchangeToken,
            callId = callId,
            ytelse = ytelse
        )

        val arbeidsgivereOppslagRespons = Retry.retry(
            operation = HENTE_ARBEIDSGIVERE_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "k9-brukerdialog-api",
                operation = HENTE_ARBEIDSGIVERE_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<ArbeidsgivereOppslagRespons>(success) },
                { error ->
                    logger.error(
                        "Error response = '${
                            error.response.body().asString(MediaTypeUtils.TEXT_PLAIN)
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av arbeidsgiver.")
                }
            )
        }
        return arbeidsgivereOppslagRespons.arbeidsgivere
    }
}
