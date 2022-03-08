package no.nav.k9brukerdialogapi.mellomlagring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.MELLOMLAGRING_URL
import no.nav.k9brukerdialogapi.ytelse.Ytelse

fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {
    route("$MELLOMLAGRING_URL"){
        post{
            mellomlagringService.setMellomlagring(
                Ytelse.valueOf(call.parameters["ytelse"]!!),
                idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer(),
                call.receive<String>()
            )
            call.respond(HttpStatusCode.NoContent)
        }

        put{
            mellomlagringService.updateMellomlagring(
                Ytelse.valueOf(call.parameters["ytelse"]!!),
                idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer(),
                call.receive<String>()
            )
            call.respond(HttpStatusCode.NoContent)
        }

        get{
            val mellomlagring = mellomlagringService.getMellomlagring(
                Ytelse.valueOf(call.parameters["ytelse"]!!),
                idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer(),
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
            mellomlagringService.deleteMellomlagring(
                Ytelse.valueOf(call.parameters["ytelse"]!!),
                idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer(),
            )
            call.respond(HttpStatusCode.Accepted)
        }
    }
}