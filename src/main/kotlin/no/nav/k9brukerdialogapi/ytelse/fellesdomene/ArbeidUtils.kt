package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import java.time.Duration

object ArbeidUtils {
    internal val NULL_ARBEIDSTIMER = Duration.ZERO
    private val DAGER_PER_UKE = 5

    internal fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)
    internal fun Double.tilDuration() = Duration.ofMinutes((this * 60).toLong())
}
