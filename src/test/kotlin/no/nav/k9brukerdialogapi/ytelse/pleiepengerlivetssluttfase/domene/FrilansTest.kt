package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.NULL_ARBEIDSTIMER
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.SYV_OG_EN_HALV_TIME
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.FULL_ARBEIDSDAG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.INGEN_ARBEIDSDAG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.fredag
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.mandag
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansTest {

    @Test
    fun `Gyldig frilans gir ingen valideringsfeil`() {
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = true
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `harHattInntektSomFrilans er null skal gi valideringsfeil`() {
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = null
        ).valider().verifiserFeil(1, listOf("frilans.harHattInntektSomFrilanser kan ikke være null."))
    }


    @Test
    fun `sluttdato før startdato skal gi valideringsfeil`() {
        Frilans(
            startdato = LocalDate.parse("2022-01-02"),
            sluttdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true
        ).valider().verifiserFeil(1, listOf("frilans.sluttdato må være lik eller etter startdato."))
    }

    @Test
    fun `Dersom jobberFortsattSomFrilans er true og sluttdato er satt skal det gi valideringsfeil`() {
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2022-01-02"),
            jobberFortsattSomFrilans = true,
            harHattInntektSomFrilanser = true
        ).valider()
            .verifiserFeil(1, listOf("frilans.sluttdato kan ikke være satt dersom jobberFortsattSomFrilans er true."))
    }

    @Test
    fun `Dersom jobberFortsattSomFrilans er false og sluttdato er null skal det gi valideringsfeil`() {
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
            arbeidsforhold = Arbeidsforhold(20.0, ArbeidIPeriode(REDUSERT, emptyList())),
        ).valider().verifiserFeil(
            1,
            listOf("frilans.arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være tom liste.")
        )
    }

    @Test
    fun `Mapping til K9Frilanser blir som forventet`() {
        val startdato = LocalDate.parse("2022-01-01")
        val sluttdato = LocalDate.parse("2022-01-10")
        Frilans(startdato, sluttdato, false, null, true)
            .somK9Frilanser().also {
                assertEquals(startdato, it.startdato)
                assertEquals(sluttdato, it.sluttdato)
            }
    }

    @Test
    fun `Mapping til K9Arbeidstid blir som forventet`() {
        val fraOgMed = mandag
        val tilOgMed = fredag
        Frilans(
            startdato = LocalDate.parse("2022-01-02"),
            sluttdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, PILSTestUtils.enkeltDagerMedFulltFravær))
        ).somK9Arbeidstid(fraOgMed, tilOgMed).also {
            assertEquals(it.perioder.size, 5)
            it.perioder.forEach { _: Periode, arbeidstidPeriodeInfo: ArbeidstidPeriodeInfo ->
                assertEquals(FULL_ARBEIDSDAG, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
                assertEquals(INGEN_ARBEIDSDAG, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
            }
        }
    }
}
