package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Frilans
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.FrilansType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansTest {

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
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
            )
        )
    }

    @Test
    fun `Frilans med valideringsfeil i arbeidsforhold`() {
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            harInntektSomFrilanser = true,
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
            .verifiserFeil(
                1,
                listOf("test.arbeidsforhold.arbeidIPeriode.prosentAvNormalt må være satt dersom type=ARBEIDER_PROSENT_AV_NORMALT")
            )
    }

    @Test
    fun `Frilans hvor harInntektSomFrilanser er true med startdato og jobberFortsattSomFrilans som null gir feil`() {
        Frilans(
            startdato = null,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        )
            .valider("test")
            .verifiserFeil(
                1, listOf(
                    "test.startdato kan ikke være null dersom harInntektSomFrilanser=true"
                )
            )
    }

    @Test
    fun `Frilans jobber som vanlig i hele søknadsperioden`() {
        val frilans = Frilans(
            startdato = LocalDate.parse("2020-01-01"),
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
    fun `Frilans uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`() {
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
    fun `Frilans som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = onsdag,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilanser som mister honorarer må ha misterHonorarerIPerioden satt`() {
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke,
            frilansTyper = listOf(FrilansType.STYREVERV, FrilansType.FRILANS),
            misterHonorarer = true,
            misterHonorarerIPerioden = null
        ).valider("test")
            .verifiserFeil(
                1, listOf(
                    "test.misterHonorarerIPerioden kan ikke være null dersom misterHonorarer=true",
                )
            )
    }
}
