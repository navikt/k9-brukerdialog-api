package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import org.json.JSONObject
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerUtvidetRettSøknadTest {

    @Test
    fun `Validering skal ikke feile på gyldig søknad`() {
        OmsorgspengerKroniskSyktBarnSøknad(
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
        assertThrows<Throwblem> {
            OmsorgspengerKroniskSyktBarnSøknad(
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
        }.also {
            assertTrue { it.message.toString().contains("Dersom sammeAdresse er false kan ikke samværsavtale være tom.") }
        }
    }

    @Test
    fun `Forventer valideringsfeil dersom harForståttRettigheterOgPlikter er false`(){
        assertThrows<Throwblem>{
            OmsorgspengerKroniskSyktBarnSøknad(
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
        }.also {
            assertTrue { it.message.toString().contains("harForståttRettigheterOgPlikter må være true") }
        }
    }

    @Test
    fun `Forventer valideringsfeil dersom harBekreftetOpplysninger er false`(){
        assertThrows<Throwblem>{
            OmsorgspengerKroniskSyktBarnSøknad(
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
        }.also {
            assertTrue { it.message.toString().contains("harBekreftetOpplysninger må være true") }
        }
    }

    @Test
    fun `Mapping av k9format blir som forventet`(){
        val søknad = OmsorgspengerKroniskSyktBarnSøknad(
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
        val faktiskK9Format = JSONObject(søknad.somK9Format(SøknadUtils.søker).somJson())
        val forventetK9Format = JSONObject(
            """
                {
                  "språk": "nb",
                  "mottattDato": "2020-01-02T03:04:05.000Z",
                  "søknadId": "${søknad.søknadId}",
                  "søker": {
                    "norskIdentitetsnummer": "02119970078"
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
                  "versjon": "1.0.0",
                  "kildesystem": "søknadsdialog"
                }
            """.trimIndent()
        )
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }
}
