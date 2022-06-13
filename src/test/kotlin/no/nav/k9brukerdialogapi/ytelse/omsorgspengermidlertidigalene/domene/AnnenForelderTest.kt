package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnnenForelderTest {

    @Test
    fun `AnnenForelder equals test`() {
        val annenForelder = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        )
        assertTrue(
            annenForelder.equals(
                AnnenForelder(
                    navn = "Navnesen",
                    fnr = "26104500284",
                    situasjon = Situasjon.FENGSEL,
                    periodeFraOgMed = LocalDate.parse("2021-01-01"),
                    periodeTilOgMed = LocalDate.parse("2021-08-01")
                )
            )
        )
        assertFalse(annenForelder.equals(null))
    }

    @Test
    fun `AnnenForelder blir mappet til forventet K9Format`(){
        val faktisk = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).somK9AnnenForelder()

        val forventet = """
            {
              "norskIdentitetsnummer": "26104500284",
              "situasjon": "FENGSEL",
              "situasjonBeskrivelse": null,
              "periode": "2021-01-01/2021-08-01"
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk.somJson(), true)
    }

    @Test
    fun `Skal gi valideringsfeile dersom navn er blankt`(){
        AnnenForelder(
            navn = " ",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.navn kan ikke være tomt eller blank."))
    }

    @Test
    fun `Skal gi valideringsfeile dersom fnr er ugyldig`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "11111111111",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.fnr er ikke gyldig identifikator, '111111*****'. kalkulertKontrollsifferEn (-) er ikke lik forventetKontrollsifferEn (1)"))
    }

    @Test
    fun `Skal gi valideringsfeile dersom fraOgMed er etter tilOgMed`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-02"),
            periodeTilOgMed = LocalDate.parse("2021-01-01")
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.periodeTilOgMed må være lik eller etter periodeFraOgMed."))
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeOver6Måneder er satt`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider("annenForelder").verifiserIngenFeil()
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeFraOgMed og periodeTilOgMed er satt`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-07-01")
        ).valider("annenForelder").verifiserIngenFeil()
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeFraOgMed=null og periodeOver6Måneder=null`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = null,
            periodeOver6Måneder = null
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.periodeTilOgMed eller periodeOver6Måneder må være satt dersom situasjonen er INNLAGT_I_HELSEINSTITUSJON"))
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon FENGSEL`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider("annenForelder").verifiserIngenFeil()
    }

    @Test
    fun `Ved situasjon FENGSEL skal det gi valideringsfeil dersom periodeTilOgMed er null`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.periodeTilOgMed kan ikke være null dersom situasjonen er FENGSEL eller UTØVER_VERNEPLIKT"))
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon UTØVER_VERNEPLIKT`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider("annenForelder").verifiserIngenFeil()
    }

    @Test
    fun `Ved situasjon UTØVER_VERNEPLIKT skal det gi valideringsfeil dersom periodeTilOgMed er null`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.periodeTilOgMed kan ikke være null dersom situasjonen er FENGSEL eller UTØVER_VERNEPLIKT"))
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon ANNET`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "Blabla noe skjedde",
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeOver6Måneder = true
        ).valider("annenForelder").verifiserIngenFeil()
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom situasjonBeskrivelse er tom`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "",
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.situasjonBeskrivelse kan ikke være null eller tom dersom situasjon er ANNET"))
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom periodeOver6Måneder=null og periodeTilOgMed=null`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "COVID-19",
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null,
            periodeOver6Måneder = null
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.periodeTilOgMed eller periodeOver6Måneder må være satt dersom situasjonen er ANNET"))
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon SYKDOM`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "Blabla noe skjedde",
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeOver6Måneder = true
        ).valider("annenForelder").verifiserIngenFeil()
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom situasjonBeskrivelse er tom`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "",
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.situasjonBeskrivelse kan ikke være null eller tom dersom situasjon er SYKDOM"))
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom periodeOver6Måneder ikke er satt`(){
        AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "COVID-19",
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider("annenForelder")
            .verifiserFeil(1, listOf("annenForelder.periodeTilOgMed eller periodeOver6Måneder må være satt dersom situasjonen er SYKDOM"))
    }

}