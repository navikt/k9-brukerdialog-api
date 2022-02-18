package no.nav.k9brukerdialogapi.oppslag

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.k9brukerdialogapi.general.CallId
import java.net.URI

fun genererOppslagHttpRequest(
    baseUrl: URI,
    attributter: Pair<String, List<String>>,
    idToken: IdToken,
    callId: CallId
): Request {
    return Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("meg"),
        queryParameters = mapOf(attributter)
    ).toString()
        .httpGet()
        .header(
            HttpHeaders.Authorization to "Bearer ${idToken.value}",
            HttpHeaders.Accept to "application/json",
            HttpHeaders.XCorrelationId to callId.value
        )
}