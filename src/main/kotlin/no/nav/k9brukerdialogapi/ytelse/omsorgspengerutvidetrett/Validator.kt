package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad

fun Søknad.valider(){
    val valideringsfeil = mutableSetOf<Violation>()

    // TODO: 04/03/2022 Hva er krav rundt samværsavtale/legerklæring?

    valideringsfeil.addAll(barn.valider())

    if(sammeAdresse != null && !sammeAdresse && samværsavtale.isNullOrEmpty()){
        valideringsfeil.add(
            Violation(
                parameterName = "sammeAdresse og samværsavtale",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom sammeAdresse er false kan ikke samværsavtale være null eller tom.",
                invalidValue = "sammeAdresse=$sammeAdresse, samværsavtale=$samværsavtale"

            )
        )
    }

    if (!harBekreftetOpplysninger) {
        valideringsfeil.add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = false

            )
        )
    }

    if (!harForståttRettigheterOgPlikter) {
        valideringsfeil.add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = false
            )
        )
    }

    if (valideringsfeil.isNotEmpty()) throw Throwblem(ValidationProblemDetails(valideringsfeil))
}

fun validerK9Format(k9Format: no.nav.k9.søknad.Søknad) {
    val valideringsfeil = OmsorgspengerKroniskSyktBarn
        .MinValidator()
        .valider(k9Format.getYtelse<OmsorgspengerKroniskSyktBarn>()).map {
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