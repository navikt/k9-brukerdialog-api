package no.nav.k9brukerdialogapi.ytelse.ettersending

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class EttersendingSøknadTest {

    @Test
    fun `Mapping av K9Format blir som forventet`(){
        val søknad = Søknad(
            språk = "nb",
            mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
            beskrivelse = "Pleiepenger .....",
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        val forventetK9Format = """
            {
              "søknadId": "${søknad.søknadId}",
              "versjon": "0.0.1",
              "mottattDato": "2020-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "26104500284"
              },
              "ytelse": "PLEIEPENGER_LIVETS_SLUTTFASE"
            }
        """.trimIndent()
        val faktiskK9Format = søknad.somK9Format(søker).somJson()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)

    }

    @Test
    fun `Gyldig søknad gir ingen valideringsfeil`() {
        Søknad(
            språk = "nb",
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
            beskrivelse = "Pleiepenger .....",
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider()
    }

    @Test
    fun `Forventer valideringsfeil dersom søknadstype er PP og beskrivelse er null`(){
        val feil: String = assertThrows<Throwblem>{
            Søknad(
                språk = "nb",
                vedlegg = listOf(),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                beskrivelse = null,
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Beskrivelse kan ikke være tom, null eller blank dersom det gjelder pleiepenger."))
    }

    @Test
    fun `Forventer valideringsfeil dersom vedlegg er tom liste`(){
        val feil: String = assertThrows<Throwblem>{
            Søknad(
                språk = "nb",
                vedlegg = listOf(),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Liste over vedlegg kan ikke være tom."))
    }

    @Test
    fun `Forventer valideringsfeil dersom harForståttRettigheterOgPlikter er false`(){
        val feil: String = assertThrows<Throwblem>{
            Søknad(
                språk = "nb",
                vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = false
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Må ha forstått rettigheter og plikter for å sende inn søknad."))
    }

    @Test
    fun `Forventer valideringsfeil dersom harBekreftetOpplysninger er false`(){
        val feil: String = assertThrows<Throwblem>{
            Søknad(
                språk = "nb",
                vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Opplysningene må bekreftes for å sende inn søknad."))
    }

}