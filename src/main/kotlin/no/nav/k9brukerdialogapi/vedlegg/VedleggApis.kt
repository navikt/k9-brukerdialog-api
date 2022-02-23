package no.nav.k9brukerdialogapi.vedlegg

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import no.nav.k9brukerdialogapi.VEDLEGGID_URL
import no.nav.k9brukerdialogapi.VEDLEGG_URL
import no.nav.k9brukerdialogapi.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.vedleggApis")
private const val MAX_VEDLEGG_SIZE = 8 * 1024 * 1024

fun Route.vedleggApis(
    vedleggService: VedleggService,
    idTokenProvider: IdTokenProvider
) {
    route(VEDLEGG_URL) {
        post {
            logger.info("Lagrer vedlegg")
            if (!call.request.isFormMultipart()) return@post call.respondProblemDetails(
                hasToBeMultipartTypeProblemDetails
            )

            val eier = idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer()
            val vedlegg: Vedlegg = call.receiveMultipart().getVedlegg(DokumentEier(eier))
                ?: return@post call.respondProblemDetails(vedleggNotAttachedProblemDetails)

            if (!vedlegg.isSupportedContentType()) return@post call.respondProblemDetails(
                vedleggContentTypeNotSupportedProblemDetails
            )
            if (vedlegg.content.size > MAX_VEDLEGG_SIZE) return@post call.respondProblemDetails(
                vedleggTooLargeProblemDetails
            )
            val vedleggId = vedleggService.lagreVedlegg(
                vedlegg = vedlegg,
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )
            logger.info("Lagret vedlegg med id:$vedleggId")
            call.respondVedlegg(VedleggId(vedleggId))
        }

        get(VEDLEGGID_URL){
            val vedleggId = VedleggId(call.parameters["vedleggId"]!!)
            logger.info("Henter vedlegg med id:${vedleggId.value}")

            val vedlegg = vedleggService.hentVedlegg(
                vedleggId = vedleggId.value,
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId(),
                eier = DokumentEier(idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer())
            )

            when(vedlegg){
                null -> call.respondProblemDetails(vedleggNotFoundProblemDetails)
                else -> call.respondBytes(bytes = vedlegg.content, contentType = ContentType.parse(vedlegg.contentType), status = HttpStatusCode.OK
                )
            }
        }

        delete(VEDLEGGID_URL) {
            val vedleggId = VedleggId(call.parameters["vedleggId"]!!)
            logger.info("Sletter vedlegg med id:${vedleggId.value}")

            val resultat = vedleggService.slettVedlegg(
                vedleggId = vedleggId.value,
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId(),
                eier = DokumentEier(idTokenProvider.getIdToken(call).getNorskIdentifikasjonsnummer())
            )

            when (resultat) {
                true -> call.respond(HttpStatusCode.NoContent)
                false -> call.respondProblemDetails(feilVedSlettingAvVedlegg)
            }
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
    val url = URLBuilder(getBaseUrlFromRequest()).path("vedlegg",vedleggId.value).build().toString()
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
