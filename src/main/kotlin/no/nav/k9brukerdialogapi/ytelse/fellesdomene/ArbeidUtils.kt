package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

object ArbeidUtils {
    internal val NULL_ARBEIDSTIMER = Duration.ZERO
    internal val SYV_OG_EN_HALV_TIME = Duration.ofHours(7).plusMinutes(30)
    private val DAGER_PER_UKE = 5

    internal fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)
    internal fun Double.tilDuration() = Duration.ofMinutes((this * 60).toLong())

    internal fun arbeidstidInfoMedNullTimer(fraOgMed: LocalDate, tilOgMed: LocalDate) = ArbeidstidInfo().medPerioder(
        mapOf(
            Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(NULL_ARBEIDSTIMER)
                .medJobberNormaltTimerPerDag(NULL_ARBEIDSTIMER)
        )
    )
}
