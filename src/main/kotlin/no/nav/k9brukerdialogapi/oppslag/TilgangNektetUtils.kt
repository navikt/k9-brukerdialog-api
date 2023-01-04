package no.nav.k9brukerdialogapi.oppslag

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.path
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils
import org.slf4j.Logger
import java.net.URI

data class TilgangNektetException(override val message: String) : RuntimeException(message)

fun FuelError.throwable(request: Request, logger: Logger, errorMessage: String): Throwable {
    val errorResponseBody = response.body().asString(MediaTypeUtils.TEXT_PLAIN)
    logger.error("Error response = '$errorResponseBody' fra '${request.url}'")
    logger.error(toString())
    return when (response.statusCode) {
        451 -> TilgangNektetException("Tilgang nektet.")
        else -> {
            IllegalStateException(errorMessage)
        }
    }
}

suspend fun ApplicationCall.respondTilgangNektetProblemDetail(logger: Logger, e: TilgangNektetException) =
    respondProblemDetails(
        logger = logger,
        problemDetails = DefaultProblemDetails(
            title = "tilgangskontroll-feil",
            status = 451,
            instance = URI(request.path()),
            detail = e.message
        )
    )

