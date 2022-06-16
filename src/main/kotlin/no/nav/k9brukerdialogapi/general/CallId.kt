package no.nav.k9brukerdialogapi.general

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*

data class CallId(val value : String)

fun ApplicationCall.getCallId() : CallId {
    return CallId(callId!!)
}