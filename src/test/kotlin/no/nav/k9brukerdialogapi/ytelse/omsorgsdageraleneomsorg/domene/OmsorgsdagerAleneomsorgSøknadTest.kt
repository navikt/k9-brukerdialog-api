package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.somJson
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OmsorgsdagerAleneomsorgSøknadTest {

    @Test
    fun `Oppdatering av identitetsnummer på barn fungerer`() {
        val søknad = Søknad(
            barn = listOf(
                Barn(
                    navn = "Barn1",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "123",
                    identitetsnummer = null,
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                ),
                Barn(
                    navn = "Barn2",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "1234",
                    identitetsnummer = null,
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                )
            ),
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )
        assertTrue(søknad.manglerIdentifikatorPåBarn())
        val barnFraOppslag = listOf(
            BarnOppslag(
                fødselsdato = LocalDate.now(),
                fornavn = "Barn1",
                mellomnavn = null,
                etternavn = "Barnesen",
                aktørId = "123",
                identitetsnummer = "25058118020"
            ),
            BarnOppslag(
                fødselsdato = LocalDate.now(),
                fornavn = "Barn2",
                mellomnavn = null,
                etternavn = "Barnesen",
                aktørId = "1234",
                identitetsnummer = "02119970078"
            )
        )
        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag)
        assertFalse(søknad.manglerIdentifikatorPåBarn())
    }

    @Test
    fun `Søknad med to barn blir splittet opp i to ulike søknader per barn`(){
        val barn1 = Barn("Barn1", TypeBarn.FRA_OPPSLAG, "123", "12345", TidspunktForAleneomsorg.TIDLIGERE)
        val barn2 = Barn("Barn2", TypeBarn.FRA_OPPSLAG, "321", "54321", TidspunktForAleneomsorg.TIDLIGERE)

        val søknad = Søknad(
            barn = listOf(barn1, barn2),
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )

        //Mapping til k9Format skal ikke fungerer før split pga flere barn i lista
        assertThrows<IllegalArgumentException> { søknad.somK9Format(søker) }

        val split = søknad.splittTilEgenSøknadPerBarn()
        assertTrue(split.size == 2)

        val k9FormatSplit = split.map { it.somK9Format(søker).somJson() }
        assertTrue(k9FormatSplit.first().contains(barn1.somK9Barn().somJson()))
        assertFalse(k9FormatSplit.first().contains(barn2.somK9Barn().somJson()))

        assertTrue(k9FormatSplit.last().contains(barn2.somK9Barn().somJson()))
        assertFalse(k9FormatSplit.last().contains(barn1.somK9Barn().somJson()))

    }

    @Test
    fun `Mapping av K9Format blir som forventet`(){
        val søknad = Søknad(
            mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
            barn = listOf(
                Barn(
                    navn = "Barn1",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "123",
                    identitetsnummer = "25058118020",
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                )
            ),
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )

        val faktiskK9Format = søknad.somK9Format(søker).somJson()
        val forventetK9Format =
            //language=json
            """
                {
                  "søknadId": ${søknad.søknadId},
                  "versjon": "1.0.0",
                  "mottattDato": "2020-01-02T03:04:05.000Z",
                  "søker": {
                    "norskIdentitetsnummer": "02119970078"
                  },
                  "ytelse": {
                    "type": "OMP_UTV_AO",
                    "barn": {
                      "norskIdentitetsnummer": "25058118020",
                      "fødselsdato": null
                    },
                    "periode": "2021-01-01/.."
                  },
                  "språk": "nb",
                  "journalposter": [],
                  "begrunnelseForInnsending": {
                    "tekst": null
                  }
                }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }

    @Test
    fun `Gir valideringsfeil dersom harBekreftetOpplysninger og harForståttRettigheterOgPlikter er false`(){
        val feil = assertThrows<Throwblem> {
            Søknad(
                barn = listOf(
                    Barn(
                        navn = "Barn1",
                        type = TypeBarn.FRA_OPPSLAG,
                        aktørId = "123",
                        identitetsnummer = "25058118020",
                        tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                    )
                ),
                språk = "nb",
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Må ha forstått rettigheter og plikter for å sende inn søknad."))
        assertTrue(feil.contains("Opplysningene må bekreftes for å sende inn søknad."))
    }

    @Test
    fun `Gir valideringsfeil dersom liste med barn er tom`(){
        val feil = assertThrows<Throwblem> {
            Søknad(
                barn = listOf(),
                språk = "nb",
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Må ha forstått rettigheter og plikter for å sende inn søknad."))
    }
}