package no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett

import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.Søknad
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

class OmsorgspengerUtvidetRettK9FormatTest {

    @Test
    fun `Mapping av k9format blir som forventet`(){
        val mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknad = Søknad(
            språk = "nb",
            mottatt = mottatt,
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