package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Enkeltdag.Companion.finnTidForGittDato
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class ArbeidIPeriode(
    private val jobberIPerioden: JobberIPeriodeSvar,
    private val enkeltdager: List<Enkeltdag>? = null
) {
    companion object{
        internal val NULL_ARBEIDSTIMER = Duration.ZERO
    }

    internal fun valider(felt: String = "arbeidIPeriode") = mutableListOf<String>().apply {
        when(jobberIPerioden){
            JobberIPeriodeSvar.JA -> krever(!enkeltdager.isNullOrEmpty(), "$felt.enkeltdager kan ikke være null/tom når jobberIPerioden=JA.")
            JobberIPeriodeSvar.NEI -> krever(enkeltdager.isNullOrEmpty(), "$felt.enkeltdager må være null/tom når jobberIPerioden=NEI.")
        }
    }

    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate, normaltimerPerDag: Duration): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()
        when(jobberIPerioden){
            JobberIPeriodeSvar.NEI -> arbeidstidInfo.leggTilPeriode(fraOgMed, tilOgMed, normaltimerPerDag, NULL_ARBEIDSTIMER)
            JobberIPeriodeSvar.JA -> {
                fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { dato ->
                    val faktiskTimerPerDag = enkeltdager?.finnTidForGittDato(dato)?: NULL_ARBEIDSTIMER
                    arbeidstidInfo.leggTilPeriode(dato, dato, normaltimerPerDag, faktiskTimerPerDag)
                }
            }
        }
        return arbeidstidInfo
    }
}

private fun ArbeidstidInfo.leggTilPeriode(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    normalTimerPerDag: Duration,
    faktiskTimerPerDag: Duration
) {
    leggeTilPeriode(
        Periode(fraOgMed, tilOgMed),
        ArbeidstidPeriodeInfo()
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)
            .medJobberNormaltTimerPerDag(normalTimerPerDag)
    )
}

enum class JobberIPeriodeSvar { JA, NEI }

class Enkeltdag(
    private val dato: LocalDate,
    private val tid: Duration
){
    companion object{
        internal fun List<Enkeltdag>.finnTidForGittDato(dato: LocalDate) = this.find { it.dato == dato }?.tid
    }
}

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> = datesUntil(tilOgMed.plusDays(1))
    .toList()
    .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }