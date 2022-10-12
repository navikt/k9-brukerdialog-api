package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.*
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
        ArbeidIPeriode(HELT_FRAVÆR, null).valider().verifiserIngenFeil()
        ArbeidIPeriode(REDUSERT, listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))).valider().verifiserIngenFeil()
    }
    
    @Test
    fun `Hvis man oppgir at man jobber men ikke sender med dager skal det gi valideringsfeil`(){
        ArbeidIPeriode(REDUSERT, emptyList()).valider("arbeidIPeriode")
            .verifiserFeil(1, listOf("arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=REDUSERT."))
    }

    @Test
    fun `Hvis man oppgir at man ikke jobber men sender med dager skal det gi valideringsfeil`(){
        ArbeidIPeriode(HELT_FRAVÆR, listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))).valider("arbeidIPeriode")
            .verifiserFeil(1, listOf("arbeidIPeriode.enkeltdager må være null/tom når jobberIPerioden=HELT_FRAVÆR."))
    }
    
    @Test
    fun `Mapping til K9Arbeidstid når man ikke jobber i perioden skal gi null faktiskTimer`(){
        val k9Arbeidstid = ArbeidIPeriode(HELT_FRAVÆR).somK9ArbeidstidInfo(mandag, fredag, SYV_OG_HALV_TIME)

        assertEquals(SYV_OG_HALV_TIME, k9Arbeidstid.perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, k9Arbeidstid.perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Mapping til K9Arbeidstid når man jobber redusert - enkeltdager man ikke har oppgitt blir satt til 0 timer faktisk`(){
        val k9Arbeidstid = ArbeidIPeriode(REDUSERT,
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

    @Test
    fun `Mapping til K9Arbeidtid når man jobber som vanlig`(){
        ArbeidIPeriode(SOM_VANLIG,).somK9ArbeidstidInfo(mandag, fredag, SYV_OG_HALV_TIME).also {
            assertEquals(SYV_OG_HALV_TIME, it.perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
            assertEquals(SYV_OG_HALV_TIME, it.perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
        }
    }

}