package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerUtvidetRettValideringTest {

    @Test
    fun `Skal ikke feile på gyldig søknad`() {
        Søknad(
            språk = "nb",
            kroniskEllerFunksjonshemming = true,
            barn = Barn(
                norskIdentifikator = "02119970078",
                navn = "Barn Barnesen"
            ),
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            sammeAdresse = true,
            legeerklæring = listOf(),
            samværsavtale = listOf(),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider()
    }

    @Test
    fun `Forvent feil dersom sammeAdresse er false og mangler samværsavtale`(){
        val feil = Assertions.assertThrows(Throwblem::class.java) {
            Søknad(
                språk = "nb",
                kroniskEllerFunksjonshemming = true,
                barn = Barn(
                    norskIdentifikator = "02119970078",
                    navn = "Barn Barnesen"
                ),
                relasjonTilBarnet = SøkerBarnRelasjon.FAR,
                sammeAdresse = false,
                samværsavtale = listOf(),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Dersom sammeAdresse er false kan ikke samværsavtale være null eller tom."))
    }

    @Test
    fun `Forventer feil dersom harForståttRettigheterOgPlikter er false`(){
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
            Søknad(
                språk = "nb",
                kroniskEllerFunksjonshemming = true,
                barn = Barn(
                    norskIdentifikator = "02119970078",
                    navn = "Barn Barnesen"
                ),
                relasjonTilBarnet = SøkerBarnRelasjon.FAR,
                sammeAdresse = true,
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = false
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Må ha forstått rettigheter og plikter for å sende inn søknad."))
    }

    @Test
    fun `Forventer feil dersom harBekreftetOpplysninger er false`(){
        val feil: String = Assertions.assertThrows(Throwblem::class.java){
            Søknad(
                språk = "nb",
                kroniskEllerFunksjonshemming = true,
                barn = Barn(
                    norskIdentifikator = "02119970078",
                    navn = "Barn Barnesen"
                ),
                relasjonTilBarnet = SøkerBarnRelasjon.FAR,
                sammeAdresse = true,
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Opplysningene må bekreftes for å sende inn søknad."))
    }

}