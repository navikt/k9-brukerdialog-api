package no.nav.k9brukerdialogapi.vedlegg

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.net.URL

const val MAX_TOTAL_VEDLEGG_SIZE = 24 * 1024 * 1024 // 3 vedlegg på 25 MB
const val MAX_VEDLEGG_SIZE = 10 * 1024 * 1024 // Enkeltfil 10 MB

internal fun List<Vedlegg>.valider(path: String, vedleggUrler: List<URL>) {
    validerTotalStørresle()
    if (size != vedleggUrler.size) {
        throw Throwblem(
            ValidationProblemDetails(
                violations = setOf(
                    Violation(
                        parameterName = "$path",
                        parameterType = ParameterType.ENTITY,
                        reason = "Mottok referanse til ${vedleggUrler.size} vedlegg, men fant kun $size vedlegg.",
                        invalidValue = vedleggUrler
                    )
                )
            )
        )
    }
}

fun List<Vedlegg>.validerTotalStørresle() {
    val totalSize = sumOf { it.content.size }
    if (totalSize > MAX_TOTAL_VEDLEGG_SIZE) {
        throw Throwblem(vedleggTooLargeProblemDetails)
    }
}
