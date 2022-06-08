package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.somK9Bosteder
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.somK9Utenlandsopphold
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BostedOgOppholdTest {

    @Test
    fun `Gyldig bosted gir ingen feil`() {
        Bosted(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(2),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = true
        ).valider("bosted").verifiserIngenFeil()
    }

    @Test
    fun `Bosted hvor erEØSLand er null gir feil`() {
        Bosted(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(2),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = null
        ).valider("bosted").verifiserFeil(1, listOf("bosted.erEØSLand må være satt"))
    }

    @Test
    fun `Bosted hvor fraOgMed er etter tilOgMed gir feil`() {
        Bosted(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(2),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = true
        ).valider("bosted").verifiserFeil(1, listOf("bosted.fraOgMed kan ikke være etter tilOgMed"))
    }

    @Test
    fun `Bosted hvor landnavn er blank gir feil`() {
        Bosted(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(2),
            landkode = "BE",
            landnavn = " ",
            erEØSLand = true
        ).valider("bosted").verifiserFeil(1, listOf("bosted.landnavn kan ikke være blankt eller tomt. landnavn=' '"))
    }

    @Test
    fun `Bosted hvor landkode er blank gir feil`() {
        Bosted(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(2),
            landkode = " ",
            landnavn = "Belgia",
            erEØSLand = true
        ).valider("bosted").verifiserFeil(1, listOf("bosted.landkode kan ikke være blankt eller tomt. landkode=' '"))
    }

    @Test
    fun `Bosted blir til forventet K9Bosted`() {
        val bosted = Bosted(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-05"),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = true
        )
        val (periode, bostedPeriodeInfo) = bosted.somK9Bosted()
        assertEquals(LocalDate.parse("2022-01-01"), periode.fraOgMed)
        assertEquals(LocalDate.parse("2022-01-05"), periode.tilOgMed)
        assertEquals("BE", bostedPeriodeInfo.land.landkode)
    }

    @Test
    fun `Liste med bosteder blir til forventet K9Bosteder`() {
        val bosteder = listOf(
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-05"),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = true
            ),
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-15"),
                tilOgMed = LocalDate.parse("2022-01-20"),
                landkode = "FJ",
                landnavn = "Fiji",
                erEØSLand = true
            )
        )
        val k9Bosteder = bosteder.somK9Bosteder()
        val forventetK9Bosted = """
            {
              "perioder": {
                "2022-01-01/2022-01-05": {
                  "land": "BE"
                },
                "2022-01-15/2022-01-20": {
                  "land": "FJ"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Bosted, k9Bosteder.somJson(), true)
    }

    @Test
    fun `Opphold blir til forventet K9Utenlandsopphold`() {
        val opphold = Opphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-05"),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = true
        )
        val (periode, bostedPeriodeInfo) = opphold.somK9Utenlandsopphold()
        assertEquals(LocalDate.parse("2022-01-01"), periode.fraOgMed)
        assertEquals(LocalDate.parse("2022-01-05"), periode.tilOgMed)
        assertEquals("BE", bostedPeriodeInfo.land.landkode)
    }

    @Test
    fun `Liste med opphold blir til forventet K9Utenlandsopphold`() {
        val opphold = listOf(
            Opphold(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-05"),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = true
            ),
            Opphold(
                fraOgMed = LocalDate.parse("2022-01-15"),
                tilOgMed = LocalDate.parse("2022-01-20"),
                landkode = "FJ",
                landnavn = "Fiji",
                erEØSLand = true
            )
        )
        val k9Bosteder = opphold.somK9Utenlandsopphold()
        val forventetK9Utenlandsopphold = """
            {
              "perioder": {
                "2022-01-01/2022-01-05": {
                  "land": "BE",
                  "årsak": null
                },
                "2022-01-15/2022-01-20": {
                  "land": "FJ",
                  "årsak": null
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Utenlandsopphold, k9Bosteder.somJson(), true)
    }
}