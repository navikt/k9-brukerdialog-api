package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.BarnDetaljer
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Frilans
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Medlemskap
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandsoppholdIPerioden
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidsRedusert
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidsUke
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SelvstendigArbeidsforholdTest {

    companion object {
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
                type = ArbeidIPeriodeType.SOM_VANLIG
            )
        )

        fun arbeidsforholdMedRedusertSnittPerUke(fraOgMed: LocalDate, tilOgMed: LocalDate) = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.REDUSERT,
                redusertArbeid = ArbeidsRedusert(
                    type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                    arbeidsuker = listOf(
                        ArbeidsUke(
                            periode = no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Periode(
                                fraOgMed = fraOgMed,
                                tilOgMed = tilOgMed
                            ),
                            timer = Duration.ofHours(15)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Selvstendig næringsdrivende jobber som normalt i hele søknadsperioden`() {
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
                    type = ArbeidIPeriodeType.SOM_VANLIG
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
    fun `Selvstendig næringsdrivende uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`() {
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
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
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
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
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
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
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
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `sn slutter i søknadsperidoen - k9Format validering`() {
        val psbSøknad = no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Søknad(
            fraOgMed = tirsdag,
            tilOgMed = torsdag,
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                harInntektSomSelvstendig = true,
                virksomhet = Virksomhet(
                    fraOgMed = mandag.minusYears(1),
                    tilOgMed = onsdag,
                    næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                    navnPåVirksomheten = "TullOgTøys",
                    organisasjonsnummer = "825905162",
                    registrertINorge = true,
                    erNyoppstartet = true
                ),
                arbeidsforhold = arbeidsforholdMedRedusertSnittPerUke(tirsdag, onsdag)
            ),
            frilans = Frilans(harInntektSomFrilanser = false),
            mottatt = ZonedDateTime.now(),
            søknadId = UUID.randomUUID().toString(),
            språk = no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Språk.nb,
            barn = BarnDetaljer(
                fødselsnummer = "10910198357",
                fødselsdato = null,
                aktørId = "123456",
                navn = "Navn Navnesen",
            ),
            arbeidsgivere = listOf(),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                utenlandsoppholdSiste12Mnd = listOf(),
                skalBoIUtlandetNeste12Mnd = false,
                utenlandsoppholdNeste12Mnd = listOf()
            ),
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = false,
                opphold = listOf()
            ),
            ferieuttakIPerioden = null,
            opptjeningIUtlandet = listOf(),
            utenlandskNæring = listOf(),
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true,
            omsorgstilbud = null,
            newVersion = null
        )

        val psbK9FormatSøknad = psbSøknad.somK9Format(
            no.nav.k9brukerdialogapi.oppslag.søker.Søker(
                aktørId = "987654321",
                fødselsdato = LocalDate.parse("1987-01-01"),
                fødselsnummer = "29897497326",
                fornavn = "Ole",
                mellomnavn = null,
                etternavn = "Doffen"
            ), metadata
        ) as Søknad

        PleiepengerSyktBarnSøknadValidator().forsikreValidert(psbK9FormatSøknad)

        val arbeidstidInfo =
            psbK9FormatSøknad.getYtelse<PleiepengerSyktBarn>().arbeidstid.selvstendigNæringsdrivendeArbeidstidInfo.get()

        val perioder = arbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, onsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, onsdag)]!!.faktiskArbeidTimerPerDag)
    }
}
