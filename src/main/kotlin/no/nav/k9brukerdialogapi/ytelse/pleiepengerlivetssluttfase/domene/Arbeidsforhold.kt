package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.arbeidstidInfoMedNullTimer
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.tilDuration
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.tilTimerPerDag
import java.time.LocalDate

class Arbeidsforhold(
    val jobberNormaltTimer: Double,
    val arbeidIPeriode: ArbeidIPeriode
) {
    companion object{
        internal fun Arbeidsforhold?.somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
            if(this == null) return arbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
            return arbeidIPeriode.somK9ArbeidstidInfo(jobberNormaltTimer.tilTimerPerDag().tilDuration())
        }
    }

    internal fun valider(felt: String = "arbeidsforhold") = mutableListOf<String>().apply {
        addAll(arbeidIPeriode.valider("$felt.arbeidIPeriode", jobberNormaltTimer.tilTimerPerDag().tilDuration()))
    }
}
