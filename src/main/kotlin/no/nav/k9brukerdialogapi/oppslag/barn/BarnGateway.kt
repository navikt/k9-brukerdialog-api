package no.nav.k9brukerdialogapi.oppslag.barn

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

class BarnGateway(
    private val accessTokenClient: CachedAccessTokenClient,
    private val k9SelvbetjeningOppslagTokenxAudience: Set<String>,
    private val baseUrl: URI
) {
    private val logger: Logger = LoggerFactory.getLogger("no.nav.k9brukerdialogapi.oppslag.barn.BarnGateway")
    private val HENTE_BARN_OPERATION = "hente-barn"
    private val objectMapper = jacksonObjectMapper().k9SelvbetjeningOppslagKonfigurert()
    private val attributter = Pair(
        "a", listOf(
            "barn[].aktør_id",
            "barn[].fornavn",
            "barn[].mellomnavn",
            "barn[].etternavn",
            "barn[].fødselsdato",
            "barn[].identitetsnummer"
        )
    )

    suspend fun hentBarn(
        idToken: IdToken,
        callId: CallId
    ): List<BarnOppslagRespons> {
        val exchangeToken = IdToken(accessTokenClient.getAccessToken(k9SelvbetjeningOppslagTokenxAudience, idToken.value).token)
        logger.info("Utvekslet token fra {} med token fra {}.", idToken.issuer(), exchangeToken.issuer())

        val httpRequest = genererOppslagHttpRequest(
            baseUrl = baseUrl,
            pathParts = "meg",
            attributter = listOf(attributter),
            idToken = exchangeToken,
            callId = callId

        )

        val oppslagRespons = Retry.retry(
            operation = HENTE_BARN_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "omsorgspengesoknad-api",
                operation = HENTE_BARN_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<BarnOppslagResponsListe>(success) },
                { error ->
                    throw error.throwable(
                        request = request,
                        logger = logger,
                        errorMessage = "Feil ved henting av informasjon om søkers barn"
                    )
                }
            )
        }
        return oppslagRespons.barn
    }

    private data class BarnOppslagResponsListe(val barn: List<BarnOppslagRespons>)
}