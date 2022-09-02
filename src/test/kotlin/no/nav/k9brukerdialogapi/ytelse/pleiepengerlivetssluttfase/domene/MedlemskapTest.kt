package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class MedlemskapTest {

    @Test
    fun `Gyldig medlemskap gir ingen valideringsfeil`(){
        Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Utenlandsopphold(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-01"), "NLD", "Nederland")
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Utenlandsopphold(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-01"), "NLD", "Nederland")
            )
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `Medlemskap med null som svar gir valideringsfeil`(){
        Medlemskap(
            harBoddIUtlandetSiste12Mnd = null,
            skalBoIUtlandetNeste12Mnd = null,
        ).valider().verifiserFeil(2,
            listOf(
                "medlemskap.harBoddIUtlandetSiste12Mnd kan ikke være null.",
                "medlemskap.skalBoIUtlandetNeste12Mnd kan ikke være null."
            )
        )
    }

    @Test
    fun `Medlemskap med ugyldig utenlandsopphold gir valideringsfeil`(){
        Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Utenlandsopphold(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-01"), "X", "Nederland"),
                Utenlandsopphold(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-01"), "NLD", " ")
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Utenlandsopphold(LocalDate.parse("2022-01-03"), LocalDate.parse("2022-01-01"), "NLD", "Nederland")
            )
        ).valider().verifiserFeil(3,
            listOf(
                "medlemskap.utenlandsoppholdSiste12Mnd[1].landkode/landnavn.landnavn kan ikke være tomt eller blankt.",
                "medlemskap.utenlandsoppholdSiste12Mnd[0].landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "medlemskap.utenlandsoppholdNeste12Mnd[0].fraOgMed må være før eller lik tilOgMed."
            )
        )
    }

    @Test
    fun `Genererer K9Bosteder som forventet`(){
        val medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Utenlandsopphold(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-01"), "CU", "Cuba"),
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Utenlandsopphold(LocalDate.parse("2022-01-04"), LocalDate.parse("2022-01-04"), "CN", "Kina")
            )
        )
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-01": {
                  "land": "CU"
                },
                "2022-01-04/2022-01-04": {
                  "land": "CN"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, medlemskap.somK9Bosteder().somJson(), true)
    }

}