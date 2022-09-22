package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.SOM_VANLIG
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidsforholdTest {
    private val NULL_TIMER = Duration.ZERO
    private val SYV_OG_HALV_TIME = Duration.ofHours(7).plusMinutes(30)
    private val TRE_TIMER = Duration.ofHours(3)

    private val mandag = LocalDate.parse("2022-08-01")
    private val tirsdag = mandag.plusDays(1)
    private val onsdag = tirsdag.plusDays(1)
    private val torsdag = onsdag.plusDays(1)
    private val fredag = torsdag.plusDays(1)

    @Test
    fun `Gyldig Arbeidsforhold gir ingen valideringsfeil`(){
        Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, null)).valider().verifiserIngenFeil()
        Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))))
            .valider().verifiserIngenFeil()
    }

    @Test
    fun `Ved feil i arbeidIPeriode skal det gi validerignsfeil`() {
        Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, null))
            .valider()
            .verifiserFeil(1, listOf(
                "arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=JA."
            )
        )
    }

    @Test
    fun `Mapping til K9ArbeidstidInfo ved null Arbeidsforhold skal gi 0 for både normaltimer og faktisktimer`() {
        null.somK9ArbeidstidInfo(mandag, fredag).also {
            assertEquals(NULL_TIMER, it.perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, it.perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Mapping til K9ArbeidstidInfo ved Arbeidsforhold hvor man ikke jobber i perioden skal bruke oppgitt normaltid og 0 faktisk`(){
        Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, null)).somK9ArbeidstidInfo(mandag, fredag).also {
            assertEquals(SYV_OG_HALV_TIME, it.perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, it.perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Mapping til K9ArbeidstidInfo hvor enkeltdager er oppgitt - manglende dager fylles med oppgitt normal og 0 faktisk`() {
        Arbeidsforhold(37.5,
            ArbeidIPeriode(
                REDUSERT,
                listOf(Enkeltdag(mandag, TRE_TIMER), Enkeltdag(onsdag, TRE_TIMER), Enkeltdag(fredag, TRE_TIMER))
            )
        ).somK9ArbeidstidInfo(mandag, fredag).also {
            listOf(mandag, onsdag, fredag).forEach { enkeltdag ->
                assertEquals(SYV_OG_HALV_TIME, it.perioder[Periode(enkeltdag, enkeltdag)]!!.jobberNormaltTimerPerDag)
                assertEquals(TRE_TIMER, it.perioder[Periode(enkeltdag, enkeltdag)]!!.faktiskArbeidTimerPerDag)
            }
            listOf(tirsdag, torsdag).forEach { enkeltdag ->
                assertEquals(SYV_OG_HALV_TIME, it.perioder[Periode(enkeltdag, enkeltdag)]!!.jobberNormaltTimerPerDag)
                assertEquals(NULL_TIMER, it.perioder[Periode(enkeltdag, enkeltdag)]!!.faktiskArbeidTimerPerDag)
            }
        }
    }

}