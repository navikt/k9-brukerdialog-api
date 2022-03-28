package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.somJson
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OmsorgspengerUtvidetRettBarnTest {

    @Test
    fun `Barn equals test`(){
        val barn = Barn(
            norskIdentifikator = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen"
        )
        assertFalse(barn.equals(null))
        assertTrue(barn.equals(
            Barn(
                norskIdentifikator = "02119970078",
                fødselsdato = LocalDate.parse("2022-01-01"),
                aktørId = "12345",
                navn = "Barn Barnesen"
            )
        ))
    }

    @Test
    fun `Oppdatering av identifikator på barn som mangler`() {
        val barnFraOppslag = listOf(
            BarnOppslag(
                fødselsdato = LocalDate.now(),
                fornavn = "Barn",
                mellomnavn = null,
                etternavn = "Barnesen",
                aktørId = "123",
                identitetsnummer = "02119970078"
            )
        )
        val barn = Barn(navn = "Barn uten identifikator", aktørId = "123")
        assertTrue(barn.manglerIdentifikator())
        barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        assertFalse(barn.manglerIdentifikator())
    }

    @Test
    fun `Barn til k9Barn blir som forventet`() {
        val barn = Barn(
            norskIdentifikator = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen"
        )

        val forventetK9Barn = JSONObject("""
            {
              "norskIdentitetsnummer": "02119970078",
              "fødselsdato": null
            }
        """.trimIndent())

        JSONAssert.assertEquals(forventetK9Barn, JSONObject(barn.somK9Barn().somJson()), true)
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er null`(){
        val barn = Barn(
            norskIdentifikator = null,
            aktørId = "123",
            navn = "Barn"
        )
        val feil = barn.valider()
        assertEquals(feil.first().reason, "Ikke gyldig norskIdentifikator.")
        assertEquals(1, feil.size)
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er blank`(){
        val barn = Barn(
            norskIdentifikator = "02119970078",
            navn = " "
        )

        val feil = barn.valider()
        assertEquals(feil.first().reason, "Navn på barnet kan ikke være tomt, og kan maks være 100 tegn.")
        assertEquals(1, feil.size)
    }
}