package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.SOM_VANLIG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.INGEN_ARBEIDSDAG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.FULL_ARBEIDSDAG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.HALV_ARBEIDSDAG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.enkeltDagerMedFulltFravær
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.enkeltDagerMedJobbSomVanlig
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.enkeltDagerMedRedusertArbeid
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.fredag
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.mandag
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.onsdag
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.tirsdag
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.torsdag
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NULL_TIMER
import org.junit.jupiter.api.Disabled
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidIPeriodeTest {
    @Test
    fun `Gyldig ArbeidIPeriode gir ingen valideringsfeil`() {
        ArbeidIPeriode(HELT_FRAVÆR, enkeltDagerMedFulltFravær).valider(normaltimerPerDag = FULL_ARBEIDSDAG)
            .verifiserIngenFeil()
        ArbeidIPeriode(SOM_VANLIG, enkeltDagerMedJobbSomVanlig).valider(normaltimerPerDag = FULL_ARBEIDSDAG)
            .verifiserIngenFeil()
        ArbeidIPeriode(REDUSERT, enkeltDagerMedRedusertArbeid).valider(normaltimerPerDag = FULL_ARBEIDSDAG)
            .verifiserIngenFeil()
    }

    @Test
    fun `Forvent feil derom det sendes tom liste med enkeltdager`() {
        ArbeidIPeriode(HELT_FRAVÆR, emptyList()).valider(normaltimerPerDag = FULL_ARBEIDSDAG)
            .verifiserFeil(1, listOf("arbeidIPeriode.enkeltdager kan ikke være tom liste."))
    }

    @Test
    @Disabled
    fun `Forvent feil dersom HELT_FRAVÆR og enkeltdager inneholder timer med arbeid`() {
        ArbeidIPeriode(
            HELT_FRAVÆR, listOf(
                Enkeltdag(mandag, NULL_TIMER),
                Enkeltdag(tirsdag, NULL_TIMER),
                Enkeltdag(onsdag, HALV_ARBEIDSDAG),
                Enkeltdag(torsdag, NULL_TIMER),
                Enkeltdag(fredag, NULL_TIMER),
            )
        )
            .valider(normaltimerPerDag = FULL_ARBEIDSDAG)
            .verifiserFeil(
                1,
                listOf("Dersom arbeidIPeriode.jobberIPerioden er HELT_FRAVÆR, så kreves det at arbeidIPeriode.enkeltdager[2].tid er 0 timer.")
            )
    }

    @Test
    @Disabled
    fun `Forvent feil dersom SOM_VANLIG og enkeltdager inneholder timer med ingen arbeid`() {
        ArbeidIPeriode(
            SOM_VANLIG, listOf(
                Enkeltdag(mandag, FULL_ARBEIDSDAG),
                Enkeltdag(tirsdag, FULL_ARBEIDSDAG),
                Enkeltdag(onsdag, HALV_ARBEIDSDAG),
                Enkeltdag(torsdag, FULL_ARBEIDSDAG),
                Enkeltdag(fredag, INGEN_ARBEIDSDAG),
            )
        )
            .valider(normaltimerPerDag = FULL_ARBEIDSDAG)
            .verifiserFeil(
                2,
                listOf(
                    "Dersom arbeidIPeriode.jobberIPerioden er SOM_VANLIG, så kreves det at arbeidIPeriode.enkeltdager[2].tid er $FULL_ARBEIDSDAG timer per dag.",
                    "Dersom arbeidIPeriode.jobberIPerioden er SOM_VANLIG, så kreves det at arbeidIPeriode.enkeltdager[4].tid er $FULL_ARBEIDSDAG timer per dag.",
                )
            )
    }

    @Test
    fun `Mapping til K9Arbeidstid når man ikke jobber i perioden skal gi null faktiskTimer`() {
        val k9Arbeidstid = ArbeidIPeriode(HELT_FRAVÆR, enkeltDagerMedFulltFravær).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)

        assertEquals(k9Arbeidstid.perioder.size, enkeltDagerMedFulltFravær.size)
        k9Arbeidstid.perioder.forEach { _, arbeidstidPeriodeInfo ->
            assertEquals(FULL_ARBEIDSDAG, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
            assertEquals(INGEN_ARBEIDSDAG, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)

        }
    }

    @Test
    fun `Mapping av redusert arbeidstid blir mappet til k9format som forventet`() {
        val k9Arbeidstid = ArbeidIPeriode(
            REDUSERT,
            listOf(
                Enkeltdag(mandag, HALV_ARBEIDSDAG),
                Enkeltdag(tirsdag, HALV_ARBEIDSDAG),
            )
        ).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)
        assertEquals(k9Arbeidstid.perioder.size, 2)
        listOf(mandag, tirsdag).forEach {
            assertEquals(FULL_ARBEIDSDAG, k9Arbeidstid.perioder[Periode(it, it)]!!.jobberNormaltTimerPerDag)
            assertEquals(HALV_ARBEIDSDAG, k9Arbeidstid.perioder[Periode(it, it)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Mapping til K9Arbeidtid når man jobber som vanlig`() {
        val k9ArbeidstidInfo = ArbeidIPeriode(SOM_VANLIG, enkeltDagerMedJobbSomVanlig).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)
        assertEquals(k9ArbeidstidInfo.perioder.size, enkeltDagerMedJobbSomVanlig.size)

        k9ArbeidstidInfo.perioder.forEach { _, u ->
            assertEquals(FULL_ARBEIDSDAG, u.jobberNormaltTimerPerDag)
            assertEquals(FULL_ARBEIDSDAG, u.faktiskArbeidTimerPerDag)
        }
    }
}
