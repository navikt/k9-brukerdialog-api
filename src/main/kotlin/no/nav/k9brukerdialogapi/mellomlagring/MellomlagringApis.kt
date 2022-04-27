package no.nav.k9brukerdialogapi.mellomlagring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.MELLOMLAGRING_URL
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.ytelse.Ytelse

fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {
    route("$MELLOMLAGRING_URL"){
        post{
            try {
                mellomlagringService.settMellomlagring(
                    call.getCallId(),
                    idTokenProvider.getIdToken(call),
                    Ytelse.valueOf(call.parameters["ytelse"]!!),
                    call.receive()
                )
                call.respond(HttpStatusCode.Created)
            } catch (e: CacheConflictException){
                call.respondCacheConflictProblemDetails()
            }
        }

        put{
            try {
                mellomlagringService.oppdaterMellomlagring(
                    call.getCallId(),
                    idTokenProvider.getIdToken(call),
                    Ytelse.valueOf(call.parameters["ytelse"]!!),
                    call.receive()
                )
                call.respond(HttpStatusCode.NoContent)
            } catch (e: CacheNotFoundException) {
                call.respondCacheNotFoundProblemDetails()
            }
        }

        get{
            val mellomlagring = mellomlagringService.hentMellomlagring(
                call.getCallId(),
                idTokenProvider.getIdToken(call),
                Ytelse.valueOf(call.parameters["ytelse"]!!)
            )
            if (mellomlagring != null) {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = mellomlagring,
                    status = HttpStatusCode.OK
                )
            } else {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = "{}",
                    status = HttpStatusCode.OK
                )
            }
        }

        delete {
            mellomlagringService.slettMellomlagring(
                call.getCallId(),
                idTokenProvider.getIdToken(call),
                Ytelse.valueOf(call.parameters["ytelse"]!!)
            )
            call.respond(HttpStatusCode.Accepted)
        }
    }
}