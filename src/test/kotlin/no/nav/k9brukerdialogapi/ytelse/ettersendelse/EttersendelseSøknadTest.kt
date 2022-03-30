package no.nav.k9brukerdialogapi.ytelse.ettersendelse

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.ytelse.ettersendelse.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.ettersendelse.domene.Søknadstype
import org.junit.jupiter.api.Assertions
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertTrue

class EttersendelseSøknadTest {

    @Test
    fun `K9Format blir som forventet`(){
        val søknad = Søknad(
            språk = "nb",
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
            beskrivelse = "Pleiepenger .....",
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).somK9Format(søker)
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
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
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
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
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
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
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
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
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