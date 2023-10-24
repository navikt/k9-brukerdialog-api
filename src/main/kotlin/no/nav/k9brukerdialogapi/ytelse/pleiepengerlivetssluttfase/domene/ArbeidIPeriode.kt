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

enum class JobberIPeriodeSvar { SOM_VANLIG, REDUSERT, HELT_FRAVÆR }

class ArbeidIPeriode(
    private val jobberIPerioden: JobberIPeriodeSvar,
    private val enkeltdager: List<Enkeltdag>,
) {
    internal fun valider(felt: String = "arbeidIPeriode", normaltimerPerDag: Duration) = mutableListOf<String>().apply {
        krever(
            enkeltdager.isNotEmpty(),
            "$felt.enkeltdager kan ikke være tom liste."
        )

        when (jobberIPerioden) {
            JobberIPeriodeSvar.HELT_FRAVÆR -> {
                enkeltdager.mapIndexed { index, enkeltdag ->
                    krever(
                        enkeltdag.tid == NULL_ARBEIDSTIMER,
                        "Dersom $felt.jobberIPerioden er $jobberIPerioden, så kreves det at $felt.enkeltdager[$index].tid er 0 timer."
                    )
                }
            }

            JobberIPeriodeSvar.SOM_VANLIG -> {
                enkeltdager.mapIndexed { index, enkeltdag ->
                    krever(
                        enkeltdag.tid == normaltimerPerDag,
                        "Dersom $felt.jobberIPerioden er $jobberIPerioden, så kreves det at $felt.enkeltdager[$index].tid er $normaltimerPerDag timer per dag."
                    )
                }
            }

            JobberIPeriodeSvar.REDUSERT -> {}  // ingen validering
        }
    }

    internal fun somK9ArbeidstidInfo(normaltimerPerDag: Duration) =
        ArbeidstidInfo().apply { leggTilPerioderFraEnkeltdager(normaltimerPerDag, enkeltdager) }
}

private fun ArbeidstidInfo.leggTilPerioderFraEnkeltdager(
    normaltimerPerDag: Duration,
    enkeltdager: List<Enkeltdag>,
) {

    enkeltdager.forEach { enkeltdag ->
        leggTilPeriode(
            fraOgMed = enkeltdag.dato,
            tilOgMed = enkeltdag.dato,
            normalTimerPerDag = normaltimerPerDag,
            faktiskTimerPerDag = enkeltdag.tid
        )
    }
}

private fun ArbeidstidInfo.leggTilPeriode(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    normalTimerPerDag: Duration,
    faktiskTimerPerDag: Duration,
) {
    leggeTilPeriode(
        Periode(fraOgMed, tilOgMed),
        ArbeidstidPeriodeInfo()
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)
            .medJobberNormaltTimerPerDag(normalTimerPerDag)
    )
}
