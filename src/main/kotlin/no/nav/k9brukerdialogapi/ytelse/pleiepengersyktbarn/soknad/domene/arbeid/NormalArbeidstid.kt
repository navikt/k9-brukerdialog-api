package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid

import java.time.Duration

data class NormalArbeidstid (
    val timerPerUkeISnitt: Duration
) {
    internal fun timerPerDagFraSnitt(): Duration = timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)

    override fun equals(other: Any?) = this === other || other is NormalArbeidstid && this.equals(other)
    private fun equals(other: NormalArbeidstid) = this.timerPerUkeISnitt == other.timerPerUkeISnitt
}
