package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.NULL_ARBEIDSTIMER
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Enkeltdag.Companion.finnTidForGittDato
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

enum class JobberIPeriodeSvar { JA, NEI }

class ArbeidIPeriode(
    private val jobberIPerioden: JobberIPeriodeSvar,
    private val enkeltdager: List<Enkeltdag>? = null
) {
    internal fun valider(felt: String = "arbeidIPeriode") = mutableListOf<String>().apply {
        when(jobberIPerioden){
            JobberIPeriodeSvar.JA -> krever(!enkeltdager.isNullOrEmpty(), "$felt.enkeltdager kan ikke være null/tom når jobberIPerioden=JA.")
            JobberIPeriodeSvar.NEI -> krever(enkeltdager.isNullOrEmpty(), "$felt.enkeltdager må være null/tom når jobberIPerioden=NEI.")
        }
    }

    internal fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate, normaltimerPerDag: Duration) = ArbeidstidInfo().apply {
        when (jobberIPerioden) {
            JobberIPeriodeSvar.NEI -> leggTilPeriode(fraOgMed, tilOgMed, normaltimerPerDag, NULL_ARBEIDSTIMER)
            JobberIPeriodeSvar.JA -> leggTilPerioderFraEnkeltdager(fraOgMed, tilOgMed, normaltimerPerDag, enkeltdager)
        }
    }
}

private fun ArbeidstidInfo.leggTilPerioderFraEnkeltdager(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    normaltimerPerDag: Duration,
    enkeltdager: List<Enkeltdag>?
) {
    fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { dato ->
        val faktiskTimerPerDag = enkeltdager?.finnTidForGittDato(dato) ?: NULL_ARBEIDSTIMER
        leggTilPeriode(dato, dato, normaltimerPerDag, faktiskTimerPerDag)
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

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> = datesUntil(tilOgMed.plusDays(1))
    .toList()
    .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }