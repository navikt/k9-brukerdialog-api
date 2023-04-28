package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.SøknadUtils
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Frilans
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.FrilansType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.HonorarerIPerioden
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format.byggK9OpptjeningAktivitet
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format.tilK9Frilanser
import java.time.Duration
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FrilansTest {

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
    fun `Frilans med valideringsfeil i arbeidsforhold`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            frilansTyper = listOf(FrilansType.FRILANS),
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    timerPerUkeISnitt = syvOgEnHalvTime
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG,
                   prosentAvNormalt = null
                )
            )
        )
            .valider("test")
            .verifiserFeil(1, listOf("test.arbeidsforhold.arbeidIPeriode.prosentAvNormalt må være satt dersom type=ARBEIDER_PROSENT_AV_NORMALT"))
    }

    @Test
    fun `Frilans hvor sluttdato er før startdato skal gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            frilansTyper = listOf(FrilansType.FRILANS),
            arbeidsforhold = null
        )
            .valider("test")
            .verifiserFeil(1, listOf("test.sluttdato kan ikke være etter startdato"))
    }

    @Test
    fun `Frilans hvor sluttdato og startdato er lik skal ikke gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = LocalDate.parse("2020-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            frilansTyper = listOf(FrilansType.FRILANS),
            arbeidsforhold = null
        ).valider("test").verifiserIngenFeil()
    }

    @Test
    fun `Frilans hvor sluttdato er etter startdato skal ikke gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = LocalDate.parse("2021-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            frilansTyper = listOf(FrilansType.FRILANS),
            arbeidsforhold = null
        ).valider("test").verifiserIngenFeil()
    }

    @Test
    fun `Frilans hvor frilansyper inneholder FRILANS krever startdato og jobberFortsattSomFrilans`(){
        Frilans(
            startdato = null,
            jobberFortsattSomFrilans = null,
            sluttdato = LocalDate.parse("2029-01-01"),
            frilansTyper = listOf(FrilansType.FRILANS),
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        )
            .valider("test")
            .verifiserFeil(2, listOf(
                "test.startdato kan ikke være null dersom søker har frilans",
                "test.jobberFortsattSomFrilans kan ikke være null dersom frilanstyper inneholder FRILANS"
            ))
    }

    @Test
    fun `Frilans hvor frilansyper inneholder STYREVERV og mister honorarer krever startdato og jobberFortsattSomFrilans`(){
        Frilans(
            startdato = null,
            jobberFortsattSomFrilans = null,
            sluttdato = LocalDate.parse("2029-01-01"),
            frilansTyper = listOf(FrilansType.STYREVERV),
            misterHonorarer = true,
            misterHonorarerIPerioden = HonorarerIPerioden.MISTER_ALLE_HONORARER,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        )
            .valider("test")
            .verifiserFeil(2, listOf(
                "test.startdato kan ikke være null dersom søker kun har styreverv og mister honorarer",
                "test.jobberFortsattSomFrilans kan ikke være null dersom søker kun har styreverv og mister honorarer"
            ))
    }

    @Test
    fun `Frilans jobber som vanlig i hele søknadsperioden`(){
        val frilans = Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`(){
        val frilans = Frilans(
            harInntektSomFrilanser = false
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = mandag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet siste dag i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet etter søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, torsdag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = onsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag )]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = tirsdag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(3, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans med tom frilansTyper gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.now(),
            sluttdato = LocalDate.parse("2029-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            frilansTyper = listOf()
        )
            .valider("test")
            .verifiserFeil(1, listOf(
                "test.frilansTyper kan ikke være tom dersom den er ulik null",
            ))
    }
    @Test
    fun `Frilans med styreverv som mister honorarer, men misterHonorarerIPerioden er null, gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.now(),
            sluttdato = LocalDate.parse("2029-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            frilansTyper = listOf(FrilansType.FRILANS, FrilansType.STYREVERV),
            misterHonorarer = true,
            misterHonorarerIPerioden = null
        )
            .valider("test")
            .verifiserFeil(1, listOf(
                "test.misterHonorarerIPerioden kan ikke være null dersom frilansTyper inneholder STYREVERV og misterHonorarer er true",
            ))
    }
    @Test
    fun `Frilans med styreverv som ikke mister honorarer, men misterHonorarerIPerioden er ulik null, gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.now(),
            sluttdato = LocalDate.parse("2029-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            frilansTyper = listOf(FrilansType.FRILANS, FrilansType.STYREVERV),
            misterHonorarer = false,
            misterHonorarerIPerioden = HonorarerIPerioden.MISTER_DELER_AV_HONORARER
        )
            .valider("test")
            .verifiserFeil(1, listOf(
                "test.misterHonorarerIPerioden må være null dersom frilansTyper inneholder STYREVERV og misterHonorarer er false",
            ))
    }
    @Test
    fun `Frilans uten styreverv som oppgir å miste honorarer gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.now(),
            sluttdato = LocalDate.parse("2029-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            frilansTyper = listOf(FrilansType.FRILANS),
            misterHonorarer = true,
            misterHonorarerIPerioden = HonorarerIPerioden.MISTER_DELER_AV_HONORARER
        )
            .valider("test")
            .verifiserFeil(2, listOf(
                "test.misterHonorarer må være null dersom frilansTyper ikke inneholder STYREVERV",
                "test.misterHonorarerIPerioden må være null dersom frilansTyper ikke inneholder STYREVERV",
            ))
    }
    @Test
    fun `Frilans som kun har styreverv skal ikke mappes til k9Format`(){
        val frilans = Frilans(
            startdato = null,
            sluttdato = null,
            harInntektSomFrilanser = true,
            frilansTyper = listOf(FrilansType.STYREVERV),
            misterHonorarer = true,
            misterHonorarerIPerioden = HonorarerIPerioden.MISTER_DELER_AV_HONORARER
        )

        val opptjeningAktivitet = SøknadUtils.defaultSøknad(UUID.randomUUID().toString())
            .copy(frilans = frilans)
            .byggK9OpptjeningAktivitet()

        assertNull(opptjeningAktivitet.frilanser)
    }
    @Test
    fun `Frilans som også har styreverv skal mappes til k9Format`(){
        val frilans = Frilans(
            startdato = LocalDate.now(),
            sluttdato = null,
            harInntektSomFrilanser = true,
            jobberFortsattSomFrilans = true,
            frilansTyper = listOf(FrilansType.STYREVERV, FrilansType.FRILANS),
            misterHonorarer = true,
            misterHonorarerIPerioden = HonorarerIPerioden.MISTER_DELER_AV_HONORARER
        )

        val opptjeningAktivitet = SøknadUtils.defaultSøknad(UUID.randomUUID().toString())
            .copy(frilans = frilans)
            .byggK9OpptjeningAktivitet()

        assertNotNull(opptjeningAktivitet.frilanser)
    }
}
