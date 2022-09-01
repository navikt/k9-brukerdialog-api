package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class PleietrengendeTest {


    @Test
    fun `Mapping til K9Pleietrengende blir som forventet`(){
        Pleietrengende(navn = "Ole", norskIdentitetsnummer = "06098523047")
            .somK9Pleietrengende().also {
                JSONAssert.assertEquals("""{"norskIdentitetsnummer":"06098523047","fødselsdato":null}""", JSONObject(it.somJson()), true)
            }

        Pleietrengende(navn = "Ole", fødselsdato = LocalDate.parse("2022-01-01"))
            .somK9Pleietrengende().also {
                JSONAssert.assertEquals("""{"norskIdentitetsnummer":null,"fødselsdato":"2022-01-01"}""", JSONObject(it.somJson()), true)
            }
    }

    @Test
    fun `Gyldig pleietrengende gir ingen valideringsfeil`(){
        Pleietrengende(navn = "Ole", norskIdentitetsnummer = "06098523047")
            .valider("pleietrengende").verifiserIngenFeil()
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`(){
        Pleietrengende(navn = " ", norskIdentitetsnummer = "06098523047")
            .valider("pleietrengende")
            .verifiserFeil(1, listOf("pleietrengende.navn kan ikke være tomt eller blankt."))
    }

    @Test
    fun `Fødselsdato i fremtiden skal gi valideringsfeil`() {
        Pleietrengende(
            navn = "Ole",
            fødselsdato = LocalDate.now().plusDays(1),
            årsakManglerIdentitetsnummer = BOR_I_UTLANDET
        ).valider("pleietrengende")
            .verifiserFeil(1, listOf("pleietrengende.fødselsdato kan ikke være i fremtiden."))
    }

    @Test
    fun `NorskIdentitetsnummer og årsak som null skal gi valideringsfeil`(){
        Pleietrengende(navn = "Ole", fødselsdato = LocalDate.now(), norskIdentitetsnummer = null, årsakManglerIdentitetsnummer = null)
            .valider("pleietrengende")
            .verifiserFeil(1, listOf("pleietrengende.årsakManglerIdentitetsnummer må være satt dersom norskIdentitetsnummer er null."))
    }

    @Test
    fun `NorskIdentitetsnummer og fødselsdato som null skal gi valideringsfeil`(){
        Pleietrengende(navn = "Ole", fødselsdato = null, norskIdentitetsnummer = null, årsakManglerIdentitetsnummer = BOR_I_UTLANDET)
            .valider("pleietrengende")
            .verifiserFeil(1, listOf("pleietrengende.fødselsdato må være satt dersom norskIdentitetsnummer er null."))
    }

    @Test
    fun `Ugyldig norskIdentitetsnummer skal gi valideringsfeil`(){
        Pleietrengende(navn = "Ole", norskIdentitetsnummer = "IKKE_GYLDIG")
            .valider("pleietrengende")
            .verifiserFeil(1, listOf("pleietrengende.norskIdentitetsnummer er ikke gyldig identifikator, 'IKKE_G*****'. Forventet at personidentifikator kun var siffer, men var IKKE_G****** (11)"))
    }
}