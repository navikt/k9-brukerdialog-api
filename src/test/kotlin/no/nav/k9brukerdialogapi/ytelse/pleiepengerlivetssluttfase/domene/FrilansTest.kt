package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.JA
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
}