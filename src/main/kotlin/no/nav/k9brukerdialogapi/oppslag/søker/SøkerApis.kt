package no.nav.k9brukerdialogapi.oppslag.søker

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.SØKER_URL
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.oppslag.TilgangNektetException
import no.nav.k9brukerdialogapi.oppslag.respondTilgangNektetProblemDetail
import no.nav.k9brukerdialogapi.ytelse.ytelseFraHeader
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.k9brukerdialogapi.oppslag.søker.SøkerApis")

fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {
    get(SØKER_URL) {
        try {
            val søker = søkerService.hentSøker(
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId(),
                ytelse = call.ytelseFraHeader()
            )
            call.respond(søker)
        } catch (e: Exception) {
            when (e) {
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, e)
                else -> throw e
            }
        }
    }
}

