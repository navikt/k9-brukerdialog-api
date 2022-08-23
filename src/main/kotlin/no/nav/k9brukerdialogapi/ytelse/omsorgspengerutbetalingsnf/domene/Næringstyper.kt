package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.felles.type.VirksomhetType

enum class Næringstyper {
    FISKE,
    JORDBRUK_SKOGBRUK,
    DAGMAMMA,
    ANNEN;

    internal fun somK9Virksomhetstype() = listOf(
        when (this) {
            FISKE -> VirksomhetType.FISKE
            JORDBRUK_SKOGBRUK -> VirksomhetType.JORDBRUK_SKOGBRUK
            DAGMAMMA -> VirksomhetType.DAGMAMMA
            ANNEN -> VirksomhetType.ANNEN
        }
    )
}