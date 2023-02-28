package no.nav.k9brukerdialogapi.kafka

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import no.nav.k9brukerdialogapi.general.getCallId

data class Metadata(
    val version: Int,
    val correlationId: String,
    val soknadDialogCommitSha: String? = null
)

fun ApplicationCall.getMetadata() = Metadata(
    version = 1,
    correlationId = getCallId().value,
    soknadDialogCommitSha = this.request.header("X-Brukerdialog-Git-Sha")
)
