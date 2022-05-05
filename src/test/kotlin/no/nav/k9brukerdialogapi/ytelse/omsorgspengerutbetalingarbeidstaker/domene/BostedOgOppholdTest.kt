package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Bosted.Companion.somK9Bosteder
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Bosted.Companion.somK9Utenlandsopphold
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BostedOgOppholdTest {

    @Test
    fun `Gyldig bosted gir ingen feil`(){
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = true
            )
    }

    @Test
    fun `Bosted hvor erEØSLand er null gir feil`(){
        assertThrows<IllegalArgumentException> {
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = null
            )

        }
    }

    @Test
    fun `Bosted hvor fraOgMed er etter tilOgMed gir feil`(){
        assertThrows<IllegalArgumentException> {
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().minusDays(2),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = true
            )
        }
    }

    @Test
    fun `Bosted hvor landnavn er blank gir feil`() {
        assertThrows<IllegalArgumentException> {
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "BE",
                landnavn = " ",
                erEØSLand = true
            )
        }
    }

    @Test
    fun `Bosted hvor landkode er blank gir feil`() {
        assertThrows<IllegalArgumentException> {
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = " ",
                landnavn = "Belgia",
                erEØSLand = true
            )
        }
    }

    @Test
    fun `Bosted blir til forventet K9Bosted`(){
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
    fun `Liste med bosteder blir til forventet K9Bosteder`(){
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
    fun `Opphold blir til forventet K9Utenlandsopphold`(){
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
    fun `Liste med opphold blir til forventet K9Utenlandsopphold`(){
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