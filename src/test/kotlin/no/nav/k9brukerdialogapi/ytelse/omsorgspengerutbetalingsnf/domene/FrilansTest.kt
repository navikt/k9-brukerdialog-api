package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class FrilansTest {

    @Test
    fun `Frilans blir mappet til riktig K9Frilanser`(){
        val k9Frilans = Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2022-10-01"),
            jobberFortsattSomFrilans = true
        ).somK9Frilanser().somJson()
        val forventetK9Frilanser = """
            {
                "startdato" : "2022-01-01",
                "sluttdato" : "2022-10-01"
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventetK9Frilanser), JSONObject(k9Frilans), true)
    }

    @Test
    fun `Gyldig Frilans gir ingen valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true
        ).valider("frilans").verifiserIngenFeil()
    }

    @Test
    fun `Frilans hvor sluttdato er før startdato gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2021-01-01"),
            jobberFortsattSomFrilans = true
        ).valider("frilans").verifiserFeil(1, listOf("frilans.sluttdato må være lik eller etter startdato."))
    }

    @Test
    fun `Frilans hvor jobberFortsattSomFrilans er false uten sluttdato gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = false
        ).valider("frilans").verifiserFeil(1, listOf("frilans.sluttdato kan ikke være null dersom jobberFortsattSomFrilans=false."))
    }

    @Test
    fun `Frilans hvor jobberFortsattSomFrilans er null gir valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = null
        ).valider("frilans").verifiserFeil(1, listOf("frilans.jobberFortsattSomFrilans kan ikke være null."))
    }

}