package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.Test

class BostedTest {

    @Test
    fun `Gyldig bosted gir ingen feil`(){
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "Kode",
                landnavn = "Navn",
                erEØSLand = true
            )
    }

    @Test
    fun `Bosted hvor erEØSLand er null gir feil`(){
        assertThrows<IllegalArgumentException> {
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "Kode",
                landnavn = "Navn",
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
                landkode = "Kode",
                landnavn = "Navn",
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
                landkode = "Kode",
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
                landnavn = "Land",
                erEØSLand = true
            )
        }
    }


}