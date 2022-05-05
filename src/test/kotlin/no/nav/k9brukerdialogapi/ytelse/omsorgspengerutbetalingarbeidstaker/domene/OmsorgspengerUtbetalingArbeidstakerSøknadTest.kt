package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.somJson
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class OmsorgspengerUtbetalingArbeidstakerSøknadTest {

    @Test
    fun `K9Format blir som forventet`(){
        val mottatt = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()
        val søknad = Søknad(
            søknadId = søknadId,
            mottatt = mottatt,
            språk = "nb",
            vedlegg = listOf(),
            bosteder = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "BE",
                    landnavn = "Belgia",
                    erEØSLand = true
                )
            ),
            opphold = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2022-01-20"),
                    tilOgMed = LocalDate.parse("2022-01-25"),
                    landkode = "BE",
                    landnavn = "Belgia",
                    erEØSLand = true
                )
            ),
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Kiwi AS",
                    organisasjonsnummer = "825905162",
                    utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                    konfliktForklaring = "Fordi blablabla",
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = true,
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = LocalDate.parse("2022-01-25"),
                            tilOgMed = LocalDate.parse("2022-01-28"),
                            årsak = FraværÅrsak.SMITTEVERNHENSYN
                        )
                    )
                )
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        )
        val faktiskK9Format = søknad.tilK9Format(SøknadUtils.søker).somJson()
        val forventetK9Format = """
            {
              "søknadId": "$søknadId",
              "versjon": "1.0.0",
              "mottattDato": "2022-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "26104500284"
              },
              "ytelse": {
                "type": "OMP_UT",
                "fosterbarn": null,
                "aktivitet": {},
                "fraværsperioder": [
                  {
                    "periode": "2022-01-25/2022-01-28",
                    "duration": null,
                    "årsak": "SMITTEVERNHENSYN",
                    "søknadÅrsak": "KONFLIKT_MED_ARBEIDSGIVER",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": "825905162"
                  }
                ],
                "fraværsperioderKorrigeringIm": null,
                "bosteder": {
                  "perioder": {
                    "2022-01-01/2022-01-10": {
                      "land": "BE"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2022-01-20/2022-01-25": {
                      "land": "BE",
                      "årsak": null
                    }
                  },
                  "perioderSomSkalSlettes": {}
                }
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
}