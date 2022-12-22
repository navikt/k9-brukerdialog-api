package no.nav.k9brukerdialogapi.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder

internal const val k9OppslagPath = "/k9-selvbetjening-oppslag-mock"
private const val k9MellomlagringPath = "/k9-mellomlagring-mock"
internal const val k9BrukerdialogCachePath = "/k9-brukerdialog-cache-mock"

internal fun WireMockBuilder.k9BrukerdialogApiConfig() = wireMockConfiguration {
    it
        .extensions(SokerResponseTransformer())
        .extensions(K9MellomlagringResponseTransformer())
        .extensions(BarnResponseTransformer())
        .extensions(ArbeidsgivereResponseTransformer())
        .extensions(K9BrukerdialogCacheResponseTransformer())
}


internal fun WireMockServer.stubK9OppslagSoker(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    responseBody: String? = null
    ) : WireMockServer {
    val responseBuilder = WireMock.aResponse()
        .withHeader("Content-Type", "application/json")
        .withStatus(statusCode.value)
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("aktør_id"))
            .withQueryParam("a", equalTo("fornavn"))
            .withQueryParam("a", equalTo("mellomnavn"))
            .withQueryParam("a", equalTo("etternavn"))
            .withQueryParam("a", equalTo("fødselsdato"))
            .willReturn(
                responseBody?.let { responseBuilder.withBody(it) }
                    ?: responseBuilder.withTransformers("k9-oppslag-soker")
            )
    )
    return this
}

internal fun WireMockServer.stubK9OppslagBarn(simulerFeil: Boolean = false): WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("barn[].aktør_id"))
            .withQueryParam("a", equalTo("barn[].fornavn"))
            .withQueryParam("a", equalTo("barn[].mellomnavn"))
            .withQueryParam("a", equalTo("barn[].etternavn"))
            .withQueryParam("a", equalTo("barn[].fødselsdato"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withTransformers("k9-oppslag-barn")
            )
    )
    return this
}

internal fun WireMockServer.stubK9OppslagArbeidsgivere(simulerFeil: Boolean = false): WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/meg.*"))
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("arbeidsgivere[].organisasjoner[].organisasjonsnummer"))
            .withQueryParam("a", equalTo("arbeidsgivere[].organisasjoner[].navn"))
            .withQueryParam("fom", AnythingPattern()) // vurder regex som validerer dato-format
            .withQueryParam("tom", AnythingPattern()) // vurder regex som validerer dato-format
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withTransformers("k9-oppslag-arbeidsgivere")
            )
    )
    return this
}

private fun WireMockServer.stubHealthEndpoint(
    vararg path : String
) : WireMockServer{
    path.forEach {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*$it")).willReturn(
                WireMock.aResponse()
                    .withStatus(200)
            )
        )
    }
    return this
}

internal fun WireMockServer.stubOppslagHealth() = stubHealthEndpoint(
    "$k9OppslagPath/health",
    "$k9BrukerdialogCachePath/health"
)

internal fun WireMockServer.stubK9Mellomlagring() : WireMockServer{
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$k9MellomlagringPath/v1/dokument.*"))
            .willReturn(
                WireMock.aResponse()
                    .withTransformers("K9MellomlagringResponseTransformer")
            )
    )
    return this
}

internal fun WireMockServer.stubK9BrukerdialogCache(): WireMockServer {
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$k9BrukerdialogCachePath.*"))
            .willReturn(
                WireMock.aResponse()
                    .withTransformers("K9BrukerdialogCacheResponseTransformer")
            )
    )
    return this
}

internal fun WireMockServer.getK9OppslagUrl() = baseUrl() + k9OppslagPath
internal fun WireMockServer.getK9MellomlagringUrl() = baseUrl() + k9MellomlagringPath + "/v1/dokument"
internal fun WireMockServer.getK9BrukerdialogCacheUrl() = baseUrl() + k9BrukerdialogCachePath
