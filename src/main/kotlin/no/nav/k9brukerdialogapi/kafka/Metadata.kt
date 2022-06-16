package no.nav.k9brukerdialogapi.kafka

import io.ktor.server.application.*
import no.nav.k9brukerdialogapi.general.getCallId

data class Metadata(
    val version : Int,
    val correlationId : String
)

fun ApplicationCall.getMetadata() = Metadata(
    version = 1,
    correlationId = getCallId().value
)