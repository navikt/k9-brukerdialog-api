package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class UtenlandsoppholdIPeriodenTest {
    @Test
    fun `Gyldig UtenlandsoppholdIPerioden gir ingen valideringsfeil`(){
        UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "NLD",
                    landnavn = "Nederland"
                )
            )
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `Opphold med feil landkode gir valideringsfeil`(){
        UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "X",
                    landnavn = "Nederland"
                )
            )
        ).valider().verifiserFeil(1,
            listOf(
                "utenlandsoppholdIPerioden.opphold[0].landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }

    @Test
    fun `Genererer forventet K9Utenlandsopphold`(){
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "NLD",
                    landnavn = "Nederland"
                )
            )
        )
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-10": {
                  "land": "NLD",
                  "årsak": null
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, utenlandsoppholdIPerioden.somK9Utenlandsopphold().somJson(), true)
    }
}