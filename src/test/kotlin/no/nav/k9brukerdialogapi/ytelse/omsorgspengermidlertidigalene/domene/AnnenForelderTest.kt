package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `Skal gi valideringsfeile dersom navn er blankt`(){
        val feil = AnnenForelder(
            navn = " ",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals("Navn på annen forelder kan ikke være null, tom eller kun white spaces", feil.first().reason)
    }

    @Test
    fun `Skal gi valideringsfeile dersom fnr er ugyldig`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "11111111111",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals("Er ikke gyldig identifikator. kalkulertKontrollsifferEn (-) er ikke lik forventetKontrollsifferEn (1)", feil.first().reason)
    }

    @Test
    fun `Skal gi valideringsfeile dersom fraOgMed er etter tilOgMed`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-02"),
            periodeTilOgMed = LocalDate.parse("2021-01-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals("periodeFraOgMed kan ikke være etter periodeTilOgMed", feil.first().reason)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeOver6Måneder er satt`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider()
        assertEquals(0, feil.size)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeFraOgMed og periodeTilOgMed er satt`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-07-01")
        ).valider()
        assertEquals(0, feil.size)
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeFraOgMed=null og periodeOver6Måneder er null`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = null,
            periodeOver6Måneder = null
        ).valider()
        assertEquals(1, feil.size)
        assertEquals(
            "periodeOver6Måneder kan ikke være null når periodeTilOgMed er null, og situasjonen er INNLAGT_I_HELSEINSTITUSJON",
            feil.first().reason
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon FENGSEL`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider()
        assertEquals(0, feil.size)
    }

    @Test
    fun `Ved situasjon FENGSEL skal det gi valideringsfeil dersom periodeTilOgMed er null`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null
        ).valider()

        assertEquals(1, feil.size)
        assertEquals("periodeTilOgMed kan ikke være null dersom situasjonen er FENGSEL", feil.first().reason)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon UTØVER_VERNEPLIKT`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).valider()
        assertEquals(0, feil.size)
    }

    @Test
    fun `Ved situasjon UTØVER_VERNEPLIKT skal det gi valideringsfeil dersom periodeTilOgMed er null`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null
        ).valider()

        assertEquals(1, feil.size)
        assertEquals("periodeTilOgMed kan ikke være null dersom situasjonen er UTØVER_VERNEPLIKT", feil.first().reason)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon ANNET`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "Blabla noe skjedde",
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeOver6Måneder = true
        ).valider()
        assertEquals(0, feil.size)
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom situasjonBeskrivelse er tom`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "",
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals(
            "Situasjonsbeskrivelse på annenForelder kan ikke være null, tom eller kun white spaces når situasjon er ANNET",
            feil.first().reason
        )
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom periodeOver6Måneder ikke er satt`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "COVID-19",
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals(
            "periodeOver6Måneder kan ikke være null når periodeTilOgMed er null, og situasjonen er ANNET",
            feil.first().reason
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon SYKDOM`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "Blabla noe skjedde",
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeOver6Måneder = true
        ).valider()
        assertEquals(0, feil.size)
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom situasjonBeskrivelse er tom`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "",
            periodeOver6Måneder = true,
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals(
            "Situasjonsbeskrivelse på annenForelder kan ikke være null, tom eller kun white spaces når situasjon er SYKDOM",
            feil.first().reason
        )
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom periodeOver6Måneder ikke er satt`(){
        val feil = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "COVID-19",
            periodeFraOgMed = LocalDate.parse("2021-01-01")
        ).valider()
        assertEquals(1, feil.size)
        assertEquals(
            "periodeOver6Måneder kan ikke være null når periodeTilOgMed er null, og situasjonen er SYKDOM",
            feil.first().reason
        )
    }
}