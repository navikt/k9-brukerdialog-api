package no.nav.k9brukerdialogapi.general

import io.ktor.application.*
import io.ktor.features.*
import no.nav.k9brukerdialogapi.kafka.Metadata

data class CallId(val value : String)

fun ApplicationCall.getCallId() : CallId {
    return CallId(callId!!)
}

fun ApplicationCall.getMetadata() = Metadata(
    version = 1,
    correlationId = getCallId().value
)
