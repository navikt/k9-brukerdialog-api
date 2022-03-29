package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerUtvidetRettSøknadTest {

    @Test
    fun `Validering skal ikke feile på gyldig søknad`() {
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
    fun `Forvent valideringsfeil dersom sammeAdresse er false og mangler samværsavtale`(){
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
    fun `Forventer valideringsfeil dersom harForståttRettigheterOgPlikter er false`(){
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
    fun `Forventer valideringsfeil dersom harBekreftetOpplysninger er false`(){
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

    @Test
    fun `Mapping av k9format blir som forventet`(){
        val søknad = Søknad(
            språk = "nb",
            mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
            barn = Barn(
                norskIdentifikator = "02119970078",
                fødselsdato = null,
                aktørId = null,
                navn = "Barn Barnsen"
            ),
            sammeAdresse = true,
            relasjonTilBarnet = SøkerBarnRelasjon.FOSTERFORELDER,
            kroniskEllerFunksjonshemming = true,
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )
        val faktiskK9Format = JSONObject(søknad.tilK9Format(SøknadUtils.søker).somJson())
        val forventetK9Format = JSONObject(
            """
                {
                  "språk": "nb",
                  "mottattDato": "2020-01-02T03:04:05.000Z",
                  "søknadId": "${søknad.søknadId}",
                  "søker": {
                    "norskIdentitetsnummer": "26104500284"
                  },
                  "ytelse": {
                    "barn": {
                      "fødselsdato": null,
                      "norskIdentitetsnummer": "02119970078"
                    },
                    "kroniskEllerFunksjonshemming": true,
                    "type": "OMP_UTV_KS"
                  },
                  "journalposter": [],
                  "begrunnelseForInnsending": {
                    "tekst": null
                  },
                  "versjon": "1.0.0"
                }
            """.trimIndent()
        )
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }

}