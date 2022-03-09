package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Barn
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerUtvidetRettValideringTest {
    val gyldigSøknad = SøknadUtils.gyldigOmsorgspengerUtvidetRettSøknad.copy(
        barn = Barn(
            norskIdentifikator = "02119970078",
            navn = "Barn Barnsen"
        )
    )

    @Test
    fun `Skal ikke feile på gyldig søknad`() {
        gyldigSøknad.valider()
    }

    @Test
    fun `Forvent feil dersom barn mangler norskIdentifikator`(){
        val feil = Assertions.assertThrows(Throwblem::class.java){
            gyldigSøknad.copy(
                barn = Barn(
                    norskIdentifikator = null,
                    navn = "Barn Barnsen"
                )
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Ikke gyldig norskIdentifikator."))
    }

    @Test
    fun `Forvent feil dersom sammeAdresse er false og mangler samværsavtale`(){
        val feil = Assertions.assertThrows(Throwblem::class.java){
            gyldigSøknad.copy(
                sammeAdresse = false,
                samværsavtale = listOf()
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Dersom sammeAdresse er false kan ikke samværsavtale være null eller tom."))
    }

    @Test
    fun `Forventer feil dersom harForståttRettigheterOgPlikter er false`(){
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
            gyldigSøknad.copy(harForståttRettigheterOgPlikter = false).valider()
        }.message.toString()
        assertTrue(feil.contains("Må ha forstått rettigheter og plikter for å sende inn søknad."))
    }

    @Test
    fun `Forventer feil dersom harBekreftetOpplysninger er false`(){
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
            gyldigSøknad.copy(harBekreftetOpplysninger = false).valider()
        }.message.toString()
        assertTrue(feil.contains("Opplysningene må bekreftes for å sende inn søknad."))
    }

}