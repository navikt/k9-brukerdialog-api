package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

internal fun validerSamtykke(
    harForståttRettigheterOgPlikter: Boolean,
    harBekreftetOpplysninger: Boolean
) = mutableSetOf<Violation>().apply {
    if (!harForståttRettigheterOgPlikter) {
        add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad."
            )
        )
    }

    if (!harBekreftetOpplysninger) {
        add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad."
            )
        )
    }
}