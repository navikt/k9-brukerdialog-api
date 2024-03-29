package no.nav.k9brukerdialogapi

import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.headersOf
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.utils.io.streams.asInput
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.APPLICATION_PDF
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.IMAGE_JPEG
import no.nav.k9brukerdialogapi.utils.MediaTypeUtils.IMAGE_PNG
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun TestApplicationEngine.handleRequestUploadImage(
    cookie: String? = null,
    jwtToken: String? = null,
    vedlegg: ByteArray = "vedlegg/iPhone_6.jpg".fromResources().readBytes(),
    fileName: String = "iPhone_6.jpg",
    contentType: String = IMAGE_JPEG,
    expectedCode: HttpStatusCode = HttpStatusCode.Created
): String {
    val boundary = "***vedlegg***"

    handleRequest(HttpMethod.Post, VEDLEGG_URL) {
        addHeader(HttpHeaders.XCorrelationId, UUID.randomUUID().toString())
        addHeader("X-K9-Brukerdialog", "søknads-dialog")
        cookie?.let { addHeader("Cookie", cookie) }
        jwtToken?.let { addHeader("Authorization", "Bearer $jwtToken") }
        addHeader(
            HttpHeaders.ContentType,
            ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
        )
        setBody(
            boundary, listOf(
                PartData.FileItem(
                    { vedlegg.inputStream().asInput() }, {},
                    headersOf(
                        Pair(
                            HttpHeaders.ContentType,
                            listOf(contentType)
                        ),
                        Pair(
                            HttpHeaders.ContentDisposition,
                            listOf(
                                ContentDisposition.File
                                    .withParameter(ContentDisposition.Parameters.Name, "vedlegg")
                                    .withParameter(ContentDisposition.Parameters.FileName, fileName)
                                    .toString()
                            )
                        )
                    )
                )
            )
        )
    }.apply {
        assertEquals(expectedCode, response.status())
        return if (expectedCode == HttpStatusCode.Created) {
            val locationHeader = response.headers[HttpHeaders.Location]
            assertNotNull(locationHeader)
            locationHeader
        } else ""
    }
}

fun TestApplicationEngine.jpegUrl(
    cookie: String? = null,
    jwtToken: String? = null,
): String {
    return handleRequestUploadImage(
        cookie = cookie,
        jwtToken = jwtToken,
        vedlegg = "vedlegg/nav-logo.png".fromResources().readBytes(),
        fileName = "nav-logo.png",
        contentType = IMAGE_PNG
    )
}

fun TestApplicationEngine.pdUrl(
    cookie: String? = null,
    jwtToken: String? = null,
): String {
    return handleRequestUploadImage(
        cookie = cookie,
        jwtToken = jwtToken,
        vedlegg = "vedlegg/test.pdf".fromResources().readBytes(),
        fileName = "test.pdf",
        contentType = APPLICATION_PDF
    )
}
