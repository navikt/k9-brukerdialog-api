package no.nav.k9brukerdialogapi.oppslag.barn

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.BARN_URL
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.oppslag.TilgangNektetException
import no.nav.k9brukerdialogapi.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.k9brukerdialogapi.oppslag.barn.barnApis")

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    get(BARN_URL) {
        try {
            val barn = barnService.hentBarn(
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )
            call.respond(BarnOppslagListe(barn))
        } catch (e: Exception) {
            when (e) {
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, e)
                else -> throw e
            }
        }
    }
}

private data class BarnOppslagListe(val barn: List<BarnOppslag>)
