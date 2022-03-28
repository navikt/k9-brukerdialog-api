package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import org.junit.jupiter.api.Assertions
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerMidlertidigAleneSøknadTest {

    @Test
    fun `Forventer valideringsfeil dersom søknaden mangler barn`() {
        val feil = Assertions.assertThrows(Throwblem::class.java) {
            Søknad(
                id = "123456789",
                mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
                språk = "nb",
                annenForelder = AnnenForelder(
                    navn = "Berit",
                    fnr = "02119970078",
                    situasjon = Situasjon.FENGSEL,
                    situasjonBeskrivelse = "Sitter i fengsel..",
                    periodeOver6Måneder = false,
                    periodeFraOgMed = LocalDate.parse("2020-01-01"),
                    periodeTilOgMed = LocalDate.parse("2020-10-01")
                ),
                barn = listOf(),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains( "Listen over barn kan ikke være tom"))
    }

    @Test
    fun `Forventer valideringsfeil dersom harForståttRettigheterOgPlikter er false`() {
        val feil = Assertions.assertThrows(Throwblem::class.java) {
            Søknad(
                id = "123456789",
                mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
                språk = "nb",
                annenForelder = AnnenForelder(
                    navn = "Berit",
                    fnr = "02119970078",
                    situasjon = Situasjon.FENGSEL,
                    situasjonBeskrivelse = "Sitter i fengsel..",
                    periodeOver6Måneder = false,
                    periodeFraOgMed = LocalDate.parse("2020-01-01"),
                    periodeTilOgMed = LocalDate.parse("2020-10-01")
                ),
                barn = listOf(
                    Barn(
                    navn = "Ole Dole",
                    norskIdentifikator = "25058118020",
                    aktørId = null)
                ),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = false
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Må ha forstått rettigheter og plikter for å sende inn søknad."))
    }

    @Test
    fun `Forventer valideringsfeil dersom harBekreftetOpplysninger er false`() {
        val feil = Assertions.assertThrows(Throwblem::class.java) {
            Søknad(
                id = "123456789",
                mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
                språk = "nb",
                annenForelder = AnnenForelder(
                    navn = "Berit",
                    fnr = "02119970078",
                    situasjon = Situasjon.FENGSEL,
                    situasjonBeskrivelse = "Sitter i fengsel..",
                    periodeOver6Måneder = false,
                    periodeFraOgMed = LocalDate.parse("2020-01-01"),
                    periodeTilOgMed = LocalDate.parse("2020-10-01")
                ),
                barn = listOf(
                    Barn(
                    navn = "Ole Dole",
                    norskIdentifikator = "25058118020",
                    aktørId = null)
                ),
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.message.toString()
        assertTrue(feil.contains("Opplysningene må bekreftes for å sende inn søknad."))
    }

    @Test
    fun `K9Format blir som forventet`() {
        val søknad = Søknad(
            id = "123456789",
            mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
            språk = "nb",
            annenForelder = AnnenForelder(
                navn = "Berit",
                fnr = "02119970078",
                situasjon = Situasjon.FENGSEL,
                situasjonBeskrivelse = "Sitter i fengsel..",
                periodeOver6Måneder = false,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = LocalDate.parse("2020-10-01")
            ),
            barn = listOf(
                Barn(
                    navn = "Ole Dole",
                    norskIdentifikator = "25058118020",
                    aktørId = null)
            ),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
        val faktiskK9Format = søknad.tilK9Format(søker).somJson()
        val forventetK9Format =
            //language=json
            """
            {
              "søknadId": ${søknad.søknadId},
              "versjon": "1.0.0",
              "mottattDato": "2020-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "26104500284"
              },
              "språk": "nb",
              "ytelse": {
                "type": "OMP_UTV_MA",
                "barn": [
                  {
                    "norskIdentitetsnummer": "25058118020",
                    "fødselsdato": null
                  }
                ],
                "annenForelder": {
                  "norskIdentitetsnummer": "02119970078",
                  "situasjon": "FENGSEL",
                  "situasjonBeskrivelse": "Sitter i fengsel..",
                  "periode": "2020-01-01/2020-10-01"
                },
                "begrunnelse": null
              },
              "begrunnelseForInnsending" : {
                "tekst" : null
              },
              "journalposter": []
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }
}