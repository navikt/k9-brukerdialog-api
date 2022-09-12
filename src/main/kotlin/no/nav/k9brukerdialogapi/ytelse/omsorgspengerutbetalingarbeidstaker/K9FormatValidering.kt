package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetalingSøknadValidator

internal fun validerK9FormatForOMP_UT(søknad: Søknad) {
    val valideringsfeil = OmsorgspengerUtbetalingSøknadValidator()
        .valider(søknad)
        .map {
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "k9-format feilkode: ${it.feilkode}"
            )
        }.toMutableSet()

    if (valideringsfeil.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(valideringsfeil))
    }
}
