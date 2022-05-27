package no.nav.k9brukerdialogapi.general

import no.nav.helse.dusseldorf.common.Personidentifikator
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

internal fun MutableList<String>.krever(resultat: Boolean?, feilmelding: String = "") {
    if (resultat != true) this.add(feilmelding)
}

internal fun MutableList<String>.kreverIkkeNull(verdi: Any?, feilmelding: String = "") {
    if (verdi == null) this.add(feilmelding)
}

data class ValidationProblemDetails(
    val feil: List<String>
) : DefaultProblemDetails(
    title = "invalid-request-parameters",
    status = 400,
    detail = "Requesten inneholder ugyldige paramtere."
) {
    override fun asMap(): Map<String, Any> {
        return super.asMap().toMutableMap().apply {
            put("invalid_parameters", feil)
        }.toMap()
    }
}

internal fun MutableSet<Violation>.validerIdentifikator(identifikator: String?, felt: String){
    if (identifikator.isNullOrBlank()) {
        add(
            Violation(
                parameterName = felt,
                parameterType = ParameterType.ENTITY,
                reason = "Kan ikke være null eller blank.",
                invalidValue = identifikator
            )
        )
    } else {
        runCatching { Personidentifikator(identifikator) }
            .onFailure {
                add(
                    Violation(
                        parameterName = felt,
                        parameterType = ParameterType.ENTITY,
                        reason = "Er ikke gyldig identifikator.",
                        invalidValue = "${identifikator.take(6)}*****"
                    )
                )
            }
    }
}

internal fun MutableList<String>.validerIdentifikator(identifikator: String?, felt: String){
    if (identifikator.isNullOrBlank()) {
        add("$felt kan ikke være null eller blank.")
    } else {
        runCatching { Personidentifikator(identifikator) }
            .onFailure { add("$felt er ikke gyldig identifikator. ${identifikator.take(6)}*****") }
    }
}