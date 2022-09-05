package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.JA
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.NEI
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidIPeriodeTest {
    private val NULL_TIMER = Duration.ZERO
    private val SYV_OG_HALV_TIME = Duration.ofHours(7).plusMinutes(30)
    private val TRE_TIMER = Duration.ofHours(3)

    private val mandag = LocalDate.parse("2022-08-01")
    private val tirsdag = mandag.plusDays(1)
    private val onsdag = tirsdag.plusDays(1)
    private val torsdag = onsdag.plusDays(1)
    private val fredag = torsdag.plusDays(1)

    @Test
    fun `Gyldig ArbeidIPeriode gir ingen valideringsfeil`() {
        ArbeidIPeriode(NEI, null).valider().verifiserIngenFeil()
        ArbeidIPeriode(JA, listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))).valider().verifiserIngenFeil()
    }
    
    @Test
    fun `Hvis man oppgir at man jobber men ikke sender med dager skal det gi valideringsfeil`(){
        ArbeidIPeriode(JA, emptyList()).valider("arbeidIPeriode")
            .verifiserFeil(1, listOf("arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=JA."))
    }

    @Test
    fun `Hvis man oppgir at man ikke jobber men sender med dager skal det gi valideringsfeil`(){
        ArbeidIPeriode(NEI, listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))).valider("arbeidIPeriode")
            .verifiserFeil(1, listOf("arbeidIPeriode.enkeltdager må være null/tom når jobberIPerioden=NEI."))
    }
    
    @Test
    fun `Mapping til K9Arbeidstid når man ikke jobber i perioden skal gi null faktiskTimer`(){
        val k9Arbeidstid = ArbeidIPeriode(NEI).somK9ArbeidstidInfo(mandag, fredag, SYV_OG_HALV_TIME)

        assertEquals(SYV_OG_HALV_TIME, k9Arbeidstid.perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, k9Arbeidstid.perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Mapping til K9Arbeidstid når man jobber i perioden - enkeltdager man ikke har oppgitt blir satt til 0 timer faktisk`(){
        val k9Arbeidstid = ArbeidIPeriode(JA,
            listOf(
                Enkeltdag(mandag, TRE_TIMER),
                Enkeltdag(tirsdag, TRE_TIMER),
            )
        ).somK9ArbeidstidInfo(mandag, fredag, SYV_OG_HALV_TIME)
        listOf(mandag, tirsdag).forEach {
            assertEquals(SYV_OG_HALV_TIME, k9Arbeidstid.perioder[Periode(it, it)]!!.jobberNormaltTimerPerDag)
            assertEquals(TRE_TIMER, k9Arbeidstid.perioder[Periode(it, it)]!!.faktiskArbeidTimerPerDag)
        }
        listOf(onsdag, torsdag, fredag).forEach {
            assertEquals(SYV_OG_HALV_TIME, k9Arbeidstid.perioder[Periode(it, it)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, k9Arbeidstid.perioder[Periode(it, it)]!!.faktiskArbeidTimerPerDag)
        }
    }

}