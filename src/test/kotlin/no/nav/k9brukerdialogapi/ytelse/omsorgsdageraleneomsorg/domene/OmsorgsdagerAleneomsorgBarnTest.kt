package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.somJson
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OmsorgsdagerAleneomsorgBarnTest {

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
        val barn = Barn(
            navn = "Barn uten identifikator",
            aktørId = "123",
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
            dato = LocalDate.parse("2022-01-01")
        )
        assertTrue(barn.manglerIdentifikator())
        barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        assertFalse(barn.manglerIdentifikator())
    }

    @Test
    fun `Barn blir mappet til forventet K9Barn`(){
        val barn = Barn(
            navn = "Barn uten identifikator",
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
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
    fun `Skal kunne opprette barn selv uten aktørId`(){
        val barn = Barn(
            navn = "Barn uten identifikator",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        )
        val feil = barn.valider()
        assertTrue(feil.isEmpty())
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er null`(){
        val barn = Barn(
            navn = "Barn uten identifikator",
            aktørId = "123",
            identitetsnummer = null ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        )
        val feil = barn.valider()
        assertEquals(feil.first().reason, "Ikke gyldig identitetsnummer.")
        assertEquals(1, feil.size)
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er blank`(){
        val barn = Barn(
            navn = " ",
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        )

        val feil = barn.valider()
        assertEquals(feil.first().reason, "Navn på barnet kan ikke være tomt, og kan maks være 100 tegn.")
        assertEquals(1, feil.size)
    }

    @Test
    fun `Skal feile dersom tidspunktForAleneomsorg er siste 2 år, men dato er ikke satt`() {
        val barn = Barn(
            navn = "Barnesen",
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
            dato = null
        )

        val feil = barn.valider()
        assertEquals(feil.first().reason, "Barn.dato kan ikke være tom dersom tidspunktForAleneomsorg er SISTE_2_ÅRENE")
        assertEquals(1, feil.size)
    }
}