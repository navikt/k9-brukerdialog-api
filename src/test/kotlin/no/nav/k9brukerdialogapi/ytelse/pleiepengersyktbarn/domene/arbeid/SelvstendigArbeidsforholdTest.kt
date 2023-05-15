package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SelvstendigArbeidsforholdTest {

    companion object{
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
        val arbeidsforholdMedNormaltidSomSnittPerUke = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
            )
        )
    }

    @Test
    fun `Selvstendig næringsdrivende jobber som normalt i hele søknadsperioden`(){
        val selvstendig = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                fiskerErPåBladB = false,
                fraOgMed = LocalDate.parse("2021-02-07"),
                næringsinntekt = 1233123,
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = false,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                harFlereAktiveVirksomheter = false,
                erNyoppstartet = true
            ),
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )

        val k9ArbeidstidInfo = selvstendig.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`(){
        val selvstendig = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = false
        )
        val k9ArbeidstidInfo = selvstendig.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = torsdag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = mandag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet siste dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = fredag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet etter søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = fredag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, torsdag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = onsdag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende  som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = tirsdag,
                tilOgMed = torsdag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(3, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }
}
