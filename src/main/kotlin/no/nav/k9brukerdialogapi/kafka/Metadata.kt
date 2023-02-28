package no.nav.k9brukerdialogapi.kafka

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import no.nav.k9brukerdialogapi.general.getCallId
import org.slf4j.LoggerFactory

data class Metadata(
    val version: Int,
    val correlationId: String,
    val soknadDialogCommitSha: String? = null
)

fun ApplicationCall.getMetadata(): Metadata {
    val header = this.request.header("X-Brukerdialog-Git-Sha")
    val logger = LoggerFactory.getLogger("ApplicationCall.getMetadata()")
    logger.info("X-Brukerdialog-Git-Sha = $header")
    return Metadata(
        version = 1,
        correlationId = getCallId().value,
        soknadDialogCommitSha = header
    )
}
