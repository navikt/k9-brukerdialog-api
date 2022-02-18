package no.nav.k9brukerdialogapi.soker

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.SØKER_URL
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.general.oppslag.TilgangNektetException
import no.nav.k9brukerdialogapi.general.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.k9.soker.SøkerApis")

fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    get(SØKER_URL) {
        try {
            call.respond(
                søkerService.getSoker(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        } catch (e: Exception){
            when(e){
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, e)
                else -> throw e
            }
        }
    }
}

