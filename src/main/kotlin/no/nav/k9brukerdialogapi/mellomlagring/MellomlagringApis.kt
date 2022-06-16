package no.nav.k9brukerdialogapi.mellomlagring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.MELLOMLAGRING_URL
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.somJson
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
            val mellomlagringFraCache = mellomlagringService.hentMellomlagring(
                call.getCallId(),
                idTokenProvider.getIdToken(call),
                Ytelse.valueOf(call.parameters["ytelse"]!!)
            )

            val responsMellomlagring: String = when{
                mellomlagringFraCache == null -> "{}"
                mellomlagringFraCache == "{}" -> "{}"
                mellomlagringFraCache.erGammelTypeMellomlagring() -> mellomlagringFraCache
                else -> Mellomlagring(mellomlagringFraCache).somJson()
            }

            call.respondText(
                contentType = ContentType.Application.Json,
                text = responsMellomlagring,
                status = HttpStatusCode.OK
            )
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