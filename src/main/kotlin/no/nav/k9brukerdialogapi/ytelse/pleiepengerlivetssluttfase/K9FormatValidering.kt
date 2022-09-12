package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.ytelse.pls.v1.PleiepengerLivetsSluttfaseSøknadValidator

fun validerK9FormatPILS(k9Format: no.nav.k9.søknad.Søknad) {
    val mangler = PleiepengerLivetsSluttfaseSøknadValidator().valider(k9Format).map {
        Violation(
            parameterName = it.felt,
            parameterType = ParameterType.ENTITY,
            reason = it.feilmelding,
            invalidValue = "K9-format feilkode: ${it.feilkode}"
        )
    }.toMutableSet()

    if (mangler.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(mangler))
    }
}