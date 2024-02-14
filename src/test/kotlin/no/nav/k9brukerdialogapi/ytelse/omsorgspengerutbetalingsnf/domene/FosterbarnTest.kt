package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn.Companion.somK9BarnListe
import org.json.JSONArray
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class FosterbarnTest {

    @Test
    fun `Barn blir til forventet K9Barn`() {
        val k9Barn = Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.parse("2022-01-01"),
            type = TypeBarn.FRA_OPPSLAG,
            identitetsnummer = "26104500284"
        ).somK9Barn()
        val forventetK9Barn = JSONObject(
            """
                {
                    "fødselsdato" :null,
                    "norskIdentitetsnummer":"26104500284"
                }
            """.trimIndent()
        )
        JSONAssert.assertEquals(forventetK9Barn, JSONObject(k9Barn.somJson()), true)
    }

    @Test
    fun `Liste med barn blir til forventet K9BarnListe hvor kun barn fra oppslag blir mappet opp`() {
        val barn = listOf(
            Barn(
                navn = "Barnesen",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FOSTERBARN,
                identitetsnummer = "26104500284"
            ),
            Barn(
                navn = "Barnesen v2",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FOSTERBARN,
                identitetsnummer = "15121670744"
            ),
            Barn(
                navn = "Barnesen v2",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.ANNET,
                identitetsnummer = "18021839511"
            )
        )
        val forventetK9Barn = JSONArray(
            """
                [{
                    "fødselsdato" :null,
                    "norskIdentitetsnummer":"26104500284"
                },
                {
                    "fødselsdato" :null,
                    "norskIdentitetsnummer":"15121670744"
                }]
            """.trimIndent()
        )
        JSONAssert.assertEquals(forventetK9Barn, JSONArray(barn.somK9BarnListe().somJson()), true)
    }

    @Test
    fun `Gyldig barn gir ingen valideringsfeil`() {
        Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.parse("2022-01-01"),
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = null,
            identitetsnummer = "26104500284"
        ).valider("barn").verifiserIngenFeil()
    }

    @Test
    fun `Barn uten fødselsnummer gir valideringsfeil`() {
        Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.parse("2022-01-01"),
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = null,
            identitetsnummer = null
        ).valider("barn").verifiserFeil(1, listOf("barn.identitetsnummer kan ikke være null eller blank."))
    }

    @Test
    fun `Barn med blankt navn gir valideringsfeil`() {
        Barn(
            navn = " ",
            fødselsdato = LocalDate.parse("2022-01-01"),
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = null,
            identitetsnummer = "26104500284"
        ).valider("barn").verifiserFeil(1, listOf("barn.navn kan ikke være tomt eller blankt."))
    }
}
