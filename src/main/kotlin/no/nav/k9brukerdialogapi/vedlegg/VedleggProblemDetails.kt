package no.nav.k9brukerdialogapi.vedlegg

import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.APPLICATION_PDF
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.IMAGE_JPEG
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.IMAGE_PNG

val supportedContentTypes = listOf(APPLICATION_PDF, IMAGE_JPEG, IMAGE_PNG)
val hasToBeMultipartTypeProblemDetails = DefaultProblemDetails(
    title = "multipart-form-required",
    status = 400,
    detail = "Requesten må være en 'multipart/form-data' request hvor en 'part' er en fil, har 'name=vedlegg' og har Content-Type header satt."
)
val vedleggNotFoundProblemDetails = DefaultProblemDetails(
    title = "attachment-not-found",
    status = 404,
    detail = "Inget vedlegg funnet med etterspurt ID."
)
val vedleggNotAttachedProblemDetails = DefaultProblemDetails(
    title = "attachment-not-attached",
    status = 400,
    detail = "Fant ingen 'part' som er en fil, har 'name=vedlegg' og har Content-Type header satt."
)
val vedleggTooLargeProblemDetails = DefaultProblemDetails(
    title = "attachment-too-large",
    status = 413,
    detail = "vedlegget var over maks tillatt størrelse på $MAX_VEDLEGG_SIZE MB."
)
val vedleggContentTypeNotSupportedProblemDetails = DefaultProblemDetails(
    title = "attachment-content-type-not-supported",
    status = 400,
    detail = "Vedleggets type må være en av $supportedContentTypes"
)
val feilVedSlettingAvVedlegg = DefaultProblemDetails(
    title = "feil-ved-sletting",
    status = 500,
    detail = "Feil ved sletting av vedlegg"
)
