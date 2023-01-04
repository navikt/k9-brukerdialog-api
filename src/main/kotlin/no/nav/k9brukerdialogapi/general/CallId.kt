package no.nav.k9brukerdialogapi.general

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.callid.callId

data class CallId(val value : String)

fun ApplicationCall.getCallId() : CallId {
    return CallId(callId!!)
}
