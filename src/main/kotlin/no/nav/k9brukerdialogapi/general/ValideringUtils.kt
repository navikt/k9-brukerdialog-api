package no.nav.k9brukerdialogapi.general

import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails

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