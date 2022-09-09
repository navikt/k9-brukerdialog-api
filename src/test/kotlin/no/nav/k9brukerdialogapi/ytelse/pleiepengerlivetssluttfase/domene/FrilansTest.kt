package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.NULL_ARBEIDSTIMER
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.SYV_OG_EN_HALV_TIME
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.JA
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.NEI
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansTest {

    @Test
    fun `Gyldig frilans gir ingen valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = true
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `harHattInntektSomFrilans er null skal gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = null
        ).valider().verifiserFeil(1, listOf("frilans.harHattInntektSomFrilanser kan ikke være null."))
    }


    @Test
    fun `sluttdato før startdato skal gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-02"),
            sluttdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true
        ).valider().verifiserFeil(1, listOf("frilans.sluttdato må være lik eller etter startdato."))
    }

    @Test
    fun `Dersom jobberFortsattSomFrilans er true og sluttdato er satt skal det gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2022-01-02"),
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = true
        ).valider().verifiserFeil(1, listOf("frilans.sluttdato kan ikke være satt dersom jobberFortsattSomFrilans er true."))
    }

    @Test
    fun `Dersom jobberFortsattSomFrilans er false og sluttdato er null skal det gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true
        ).valider().verifiserFeil(1, listOf("frilans.sluttdato må være satt dersom jobberFortsattSomFrilans er false."))
    }

    @Test
    fun `Feil i arbeidsforhold skal gi valideringsfeil`() {
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(20.0, ArbeidIPeriode(JA, emptyList())),
        ).valider().verifiserFeil(1,
            listOf("frilans.arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=JA.")
        )
    }

    @Test
    fun `Mapping til K9Frilanser blir som forventet`(){
        val startdato = LocalDate.parse("2022-01-01")
        val sluttdato = LocalDate.parse("2022-01-10")
        Frilans(startdato, sluttdato,false, null, true)
            .somK9Frilanser().also {
                assertEquals(startdato, it.startdato)
                assertEquals(sluttdato, it.sluttdato)
            }
    }

    @Test
    fun `Mapping til K9Arbeidstid blir som forventet`(){
        val fraOgMed = LocalDate.parse("2022-01-01")
        val tilOgMed = LocalDate.parse("2022-01-10")
        Frilans(
            startdato = LocalDate.parse("2022-01-02"),
            sluttdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI))
        ).somK9Arbeidstid(fraOgMed, tilOgMed).also {
            assertEquals(SYV_OG_EN_HALV_TIME, it.perioder[Periode(fraOgMed, tilOgMed)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_ARBEIDSTIMER, it.perioder[Periode(fraOgMed, tilOgMed)]!!.faktiskArbeidTimerPerDag)
        }
    }
}