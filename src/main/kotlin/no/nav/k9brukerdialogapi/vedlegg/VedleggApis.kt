package no.nav.k9brukerdialogapi.vedlegg

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.http.path
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import no.nav.k9brukerdialogapi.VALIDERING_URL
import no.nav.k9brukerdialogapi.VEDLEGGID_URL
import no.nav.k9brukerdialogapi.VEDLEGG_URL
import no.nav.k9brukerdialogapi.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.vedleggApis")
private const val MAX_VEDLEGG_SIZE = 10 * 1024 * 1024

fun Route.vedleggApis(
    vedleggService: VedleggService,
    idTokenProvider: IdTokenProvider
) {
    route(VEDLEGG_URL) {
        post {
            logger.info("Lagrer vedlegg")
            val eier = idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer()
            val vedlegg: Vedlegg = call.receiveMultipart().getVedlegg(DokumentEier(eier))
                ?: return@post call.respondProblemDetails(vedleggNotAttachedProblemDetails)

            when {
                !call.request.isFormMultipart() -> return@post call.respondProblemDetails(
                    hasToBeMultipartTypeProblemDetails
                )
                !vedlegg.isSupportedContentType() -> return@post call.respondProblemDetails(
                    vedleggContentTypeNotSupportedProblemDetails
                )
                vedlegg.content.size > MAX_VEDLEGG_SIZE -> return@post call.respondProblemDetails(
                    vedleggTooLargeProblemDetails
                )
                else -> {
                    val vedleggId = vedleggService.lagreVedlegg(
                        vedlegg = vedlegg,
                        idToken = idTokenProvider.getIdToken(call),
                        callId = call.getCallId()
                    )
                    logger.info("Lagret vedlegg med id:$vedleggId")
                    call.respondVedlegg(VedleggId(vedleggId))
                }
            }
        }

        get(VEDLEGGID_URL){
            val vedleggId = VedleggId(call.parameters["vedleggId"]!!)
            logger.info("Henter vedlegg med id:${vedleggId.value}")

            val vedlegg = vedleggService.hentVedlegg(
                vedleggId = vedleggId.value,
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )

            when(vedlegg){
                null -> call.respondProblemDetails(vedleggNotFoundProblemDetails)
                else -> call.respondBytes(bytes = vedlegg.content, contentType = ContentType.parse(vedlegg.contentType), status = HttpStatusCode.OK)
            }
        }

        delete(VEDLEGGID_URL) {
            val vedleggId = VedleggId(call.parameters["vedleggId"]!!)
            logger.info("Sletter vedlegg med id:${vedleggId.value}")

            val resultat = vedleggService.slettVedlegg(
                vedleggId = vedleggId.value,
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )

            when (resultat) {
                true -> call.respond(HttpStatusCode.NoContent)
                false -> call.respondProblemDetails(feilVedSlettingAvVedlegg)
            }
        }


        post(VALIDERING_URL){
            val vedleggListe = call.receive<VedleggListe>()
            logger.info("Validerer at ${vedleggListe.vedleggUrl.size} vedlegg finnes.")

            val vedleggSomIkkeEksisterer = vedleggService.finnVedleggSomIkkeEksisterer(
                vedleggListe,
                idTokenProvider.getIdToken(call),
                call.getCallId()
            )

            if(vedleggSomIkkeEksisterer.isNotEmpty()) logger.info("Fant ikke ${vedleggSomIkkeEksisterer.size} vedlegg")
            call.respond(VedleggListe(vedleggSomIkkeEksisterer))
        }
    }
}

private suspend fun MultiPartData.getVedlegg(eier: DokumentEier) : Vedlegg? {
    for (partData in readAllParts()) {
        if (partData is PartData.FileItem && "vedlegg".equals(partData.name, ignoreCase = true) && partData.contentType != null) {
            val vedlegg = Vedlegg(
                content = partData.streamProvider().readBytes(),
                contentType = partData.contentType.toString(),
                title = partData.originalFileName?: "Ingen tittel tilgjengelig",
                eier = eier
            )
            partData.dispose()
            return vedlegg
        }
        partData.dispose()
    }
    return null
}

private fun Vedlegg.isSupportedContentType(): Boolean = supportedContentTypes.contains(contentType.lowercase(Locale.getDefault()))

private fun ApplicationRequest.isFormMultipart(): Boolean {
    return contentType().withoutParameters().match(ContentType.MultiPart.FormData)
}

private suspend fun ApplicationCall.respondVedlegg(vedleggId: VedleggId) {
    val url = URLBuilder(getBaseUrlFromRequest()).apply {
        path("vedlegg",vedleggId.value)
    }.build().toString()
    response.header(HttpHeaders.Location, url)
    response.header(HttpHeaders.AccessControlExposeHeaders, HttpHeaders.Location)
    respond(HttpStatusCode.Created)
}

private fun ApplicationCall.getBaseUrlFromRequest() : String {
    val host = request.origin.host
    val isLocalhost = "localhost".equals(host, ignoreCase = true)
    val scheme = if (isLocalhost) "http" else "https"
    val port = if (isLocalhost) ":${request.origin.port}" else ""
    return "$scheme://$host$port"
}
