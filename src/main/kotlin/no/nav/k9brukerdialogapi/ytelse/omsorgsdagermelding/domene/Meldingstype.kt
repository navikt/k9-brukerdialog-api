package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSDAGER_MELDING_FORDELING
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSDAGER_MELDING_KORONAOVERFORING
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSDAGER_MELDING_OVERFORING

enum class Meldingstype {
    FORDELING,
    OVERFORING,
    KORONA;

    internal fun somYtelse() = when(this){
        FORDELING -> OMSORGSDAGER_MELDING_FORDELING
        OVERFORING -> OMSORGSDAGER_MELDING_OVERFORING
        KORONA -> OMSORGSDAGER_MELDING_KORONAOVERFORING
    }
}
