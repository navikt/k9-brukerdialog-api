package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorg

fun validerK9Format(k9Format: no.nav.k9.søknad.Søknad) {
    val valideringsfeil = OmsorgspengerAleneOmsorg
        .MinValidator()
        .valider(k9Format.getYtelse<OmsorgspengerAleneOmsorg>()).map {
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "K9-format feilkode: ${it.feilkode}"
            )
        }.sortedBy { it.reason }.toMutableSet()

    if (valideringsfeil.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(valideringsfeil))
    }
}