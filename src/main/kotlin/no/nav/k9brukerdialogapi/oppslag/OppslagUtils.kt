package no.nav.k9brukerdialogapi.oppslag

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils
import java.net.URI

fun genererOppslagHttpRequest(
    pathParts: String,
    baseUrl: URI,
    attributter: List<Pair<String, List<String>>>,
    idToken: IdToken,
    callId: CallId
): Request {
    return Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf(pathParts),
        queryParameters = attributter.toMap()
    ).toString()
        .httpGet()
        .header(
            HttpHeaders.Authorization to "Bearer ${idToken.value}",
            HttpHeaders.Accept to MediaTypeUtils.APPLICATION_JSON,
            HttpHeaders.XCorrelationId to callId.value
        )
}
