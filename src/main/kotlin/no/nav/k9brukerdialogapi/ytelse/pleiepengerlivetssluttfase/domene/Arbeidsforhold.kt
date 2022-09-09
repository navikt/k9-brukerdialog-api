package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.NULL_ARBEIDSTIMER
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

            return arbeidIPeriode.somK9ArbeidstidInfo(fraOgMed, tilOgMed, jobberNormaltTimer.tilTimerPerDag().tilDuration())
        }

        internal fun arbeidstidInfoMedNullTimer(fraOgMed: LocalDate, tilOgMed: LocalDate) = ArbeidstidInfo().medPerioder(
            mapOf(
                Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                    .medFaktiskArbeidTimerPerDag(NULL_ARBEIDSTIMER)
                    .medJobberNormaltTimerPerDag(NULL_ARBEIDSTIMER)
            )
        )
    }

    internal fun valider(felt: String = "arbeidsforhold") = mutableListOf<String>().apply {
        addAll(arbeidIPeriode.valider("$felt.arbeidIPeriode"))
    }
}