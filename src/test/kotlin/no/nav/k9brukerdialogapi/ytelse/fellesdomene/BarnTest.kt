package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.somJson
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarnTest {

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
    fun `Gyldig barn gir ingen valideringsfeil`(){
        Barn(
            norskIdentifikator = "02119970078",
            navn = "Barnesen"
        ).valider("barn").verifiserIngenFeil()
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er null`(){
        Barn(
            norskIdentifikator = null,
            aktørId = "123",
            navn = "Barn"
        ).valider("barn")
            .verifiserFeil(1, listOf("barn.norskIdentifikator kan ikke være null eller blank."))
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`(){
        Barn(
            norskIdentifikator = "11111111111",
            aktørId = "123",
            navn = "Barn"
        ).valider("barn")
            .verifiserFeil(1, listOf("barn.norskIdentifikator er ikke gyldig identifikator, '111111*****'. kalkulertKontrollsifferEn (-) er ikke lik forventetKontrollsifferEn (1)"))
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er blank`(){
        Barn(
            norskIdentifikator = "02119970078",
            navn = " "
        ).valider("barn").verifiserFeil(1, listOf("barn.navn kan ikke være tomt eller blank."))
    }
}