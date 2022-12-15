package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.k9.søknad.felles.type.VirksomhetType

enum class Næringstype {
    FISKE,
    JORDBRUK_SKOGBRUK,
    DAGMAMMA,
    ANNEN;

    internal fun somK9Virksomhetstype() = when (this) {
        FISKE -> VirksomhetType.FISKE
        JORDBRUK_SKOGBRUK -> VirksomhetType.JORDBRUK_SKOGBRUK
        DAGMAMMA -> VirksomhetType.DAGMAMMA
        ANNEN -> VirksomhetType.ANNEN
    }
}
