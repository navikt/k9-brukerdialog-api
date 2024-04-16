package no.nav.k9brukerdialogapi.ytelse.ettersending

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Pleietrengende
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class EttersendingSøknadTest {

    @Test
    fun `Mapping av K9Format blir som forventet`(){
        val søknad = Ettersendelse(
            språk = "nb",
            mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
            beskrivelse = "Pleiepenger .....",
            ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
            pleietrengende = Pleietrengende(norskIdentitetsnummer = "02119970078"),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        val forventetK9Format = """
            {
              "søknadId": "${søknad.søknadId}",
              "versjon": "0.0.1",
              "mottattDato": "2020-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "type": "LEGEERKLÆRING",
              "pleietrengende": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": "PLEIEPENGER_LIVETS_SLUTTFASE"
            }
        """.trimIndent()
        val faktiskK9Format = søknad.somK9Format(søker, metadata).somJson()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)

    }

    @Test
    fun `Gyldig søknad gir ingen valideringsfeil`() {
        Ettersendelse(
            språk = "nb",
            vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
            søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
            beskrivelse = "Pleiepenger .....",
            ettersendelsesType = EttersendelseType.ANNET,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `Forventer valideringsfeil dersom søknadstype er PP og beskrivelse er null`() {
        assertThrows<Throwblem> {
            Ettersendelse(
                språk = "nb",
                vedlegg = listOf(),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                ettersendelsesType = EttersendelseType.ANNET,
                beskrivelse = null,
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("beskrivelse må være satt dersom det gjelder pleiepenger") }
        }
    }

    @Test
    fun `Forventer valideringsfeil dersom vedlegg er tom liste`(){
        assertThrows<Throwblem>{
            Ettersendelse(
                språk = "nb",
                vedlegg = listOf(),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                ettersendelsesType = EttersendelseType.ANNET,
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue(it.message.toString().contains("Liste over vedlegg kan ikke være tom"))
        }
    }

    @Test
    fun `Forventer valideringsfeil dersom harForståttRettigheterOgPlikter er false`(){
        assertThrows<Throwblem> {
            Ettersendelse(
                språk = "nb",
                vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                ettersendelsesType = EttersendelseType.ANNET,
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = false
            ).valider()
        }.also {
            assertTrue(it.message.toString().contains("harForståttRettigheterOgPlikter må være true"))
        }
    }

    @Test
    fun `Forventer valideringsfeil dersom harBekreftetOpplysninger er false`(){
        assertThrows<Throwblem>{
            Ettersendelse(
                språk = "nb",
                vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                harBekreftetOpplysninger = false,
                ettersendelsesType = EttersendelseType.ANNET,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue(it.message.toString().contains("harBekreftetOpplysninger må være true"))
        }
    }

    @Test
    fun `Forventer valideringsfeil dersom pleietrengende mangler ved ettersendelse av legeerklæring`(){
        assertThrows<Throwblem>{
            Ettersendelse(
                språk = "nb",
                vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
                ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
                pleietrengende = null,
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue(it.message.toString().contains("Pleietrengende må være satt dersom ettersendelsen gjelder legeerklæring"))
        }
    }

}
