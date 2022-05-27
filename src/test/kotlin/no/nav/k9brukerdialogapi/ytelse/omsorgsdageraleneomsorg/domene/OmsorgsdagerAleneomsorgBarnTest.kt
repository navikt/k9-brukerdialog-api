package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.TestUtils.Companion.validerFeil
import no.nav.helse.TestUtils.Companion.validerIngenFeil
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.somJson
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
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
            type = TypeBarn.FRA_OPPSLAG,
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
            type = TypeBarn.FRA_OPPSLAG,
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
    fun `Skal kunne registrere fosterbarn uten aktørId`(){
        Barn(
            navn = "Barn uten identifikator",
            type = TypeBarn.FOSTERBARN,
            fødselsdato = LocalDate.now().minusMonths(4),
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        ).valider("barn").validerIngenFeil()
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er null`(){
        Barn(
            navn = "Barn",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = null ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        ).valider("barn").validerFeil(1, listOf("barn.identitetsnummer kan ikke være null eller blank."))
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`(){
        Barn(
            navn = "Barn",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = "11111111111" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        ).valider("barn").validerFeil(1, listOf("barn.identitetsnummer er ikke gyldig identifikator, '111111*****'. kalkulertKontrollsifferEn (-) er ikke lik forventetKontrollsifferEn (1)"))
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er blank`(){
        Barn(
            navn = " ",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        ).valider("barn").validerFeil(1, listOf("barn.navn kan ikke være tomt/blankt."))
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er 101 tegn`(){
        Barn(
            navn = "barnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnb",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        ).valider("barn").validerFeil(1, listOf("barn.navn kan ikke være over 100 tegn."))
    }

    @Test
    fun `Forvent valideringsfeil dersom fødselsdato er null og barnet er fosterbarn`(){
        Barn(
            navn = "Navnesen",
            type = TypeBarn.FOSTERBARN,
            fødselsdato = null,
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        ).valider("barn").validerFeil(1, listOf("barn.fødselsdato må være satt når type!=FRA_OPPSLAG."))
    }

    @Test
    fun `Skal feile dersom tidspunktForAleneomsorg er siste 2 år, men dato er ikke satt`() {
        Barn(
            navn = "Barnesen",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = "02119970078" ,
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
            dato = null
        ).valider("barn").validerFeil(1, listOf("barn.dato må være satt."))
    }
}