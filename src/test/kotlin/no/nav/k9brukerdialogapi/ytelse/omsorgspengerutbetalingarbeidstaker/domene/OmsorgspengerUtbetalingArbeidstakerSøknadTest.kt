package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerUtbetalingArbeidstakerSøknadTest {

    @Test
    fun `K9Format blir som forventet`() {
        val mottatt = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()
        val søknad = OmsorgspengerutbetalingArbeidstakerSøknad(
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
                            årsak = FraværÅrsak.SMITTEVERNHENSYN,
                            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                        )
                    )
                )
            ),
            dineBarn = DineBarn(
                harDeltBosted = false,
                barn = listOf(
                    Barn(
                        identitetsnummer = "11223344567",
                        aktørId = "1234567890",
                        LocalDate.now(),
                        "Barn Barnesen",
                        TypeBarn.FRA_OPPSLAG
                    )
                ),
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true,
            dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
        )
        val faktiskK9Format = søknad.somK9Format(SøknadUtils.søker, metadata).somJson()
        val forventetK9Format = """
            {
              "søknadId": "$søknadId",
              "versjon": "1.1.0",
              "mottattDato": "2022-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": {
                "type": "OMP_UT",
                "fosterbarn": [],
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
                    "arbeidsgiverOrgNr": "825905162",
                    "delvisFravær": null
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
                      "årsak": null,
                      "erSammenMedBarnet": true
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "dataBruktTilUtledning": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "soknadDialogCommitSha": "abc-123",
                    "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                }
              },
              "språk": "nb",
              "journalposter": [],
              "begrunnelseForInnsending": {
                "tekst": null
              },
              "kildesystem": "søknadsdialog"
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }

    @Test
    fun `Gyldig søknad gir ingen feil`() {
        OmsorgspengerutbetalingArbeidstakerSøknad(
            språk = "nb",
            vedlegg = listOf(),
            bosteder = listOf(),
            opphold = listOf(),
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ),
            dineBarn = DineBarn(
                harDeltBosted = false,
                barn = listOf(
                    Barn(
                        identitetsnummer = "11223344567",
                        aktørId = "1234567890",
                        LocalDate.now(),
                        "Barn Barnesen",
                        TypeBarn.FRA_OPPSLAG
                    )
                ),
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
                            årsak = FraværÅrsak.SMITTEVERNHENSYN,
                            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                        )
                    )
                )
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        ).valider()
    }

    @Test
    fun `Søknad uten arbeidsgivere gir feil`() {
        assertThrows<Throwblem> {
            OmsorgspengerutbetalingArbeidstakerSøknad(
                språk = "nb",
                vedlegg = listOf(),
                bosteder = listOf(),
                opphold = listOf(),
                bekreftelser = Bekreftelser(
                    harBekreftetOpplysninger = true,
                    harForståttRettigheterOgPlikter = true
                ),
                arbeidsgivere = listOf(),
                hjemmePgaSmittevernhensyn = true,
                hjemmePgaStengtBhgSkole = true,
                dineBarn = DineBarn(
                    harDeltBosted = false,
                    barn = listOf(
                        Barn(
                            identitetsnummer = "11223344567",
                            aktørId = "1234567890",
                            LocalDate.now(),
                            "Barn Barnesen",
                            TypeBarn.FRA_OPPSLAG
                        )
                    ),
                ),
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("Må ha minst en arbeidsgiver satt.") }
        }
    }

    @Test
    fun `Gyldig søknad med dineBarn gir ingen feil`() {
        OmsorgspengerutbetalingArbeidstakerSøknad(
            språk = "nb",
            vedlegg = listOf(),
            bosteder = listOf(),
            opphold = listOf(),
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ),
            dineBarn = DineBarn(
                harDeltBosted = false,
                barn = listOf(
                    Barn(
                        identitetsnummer = "11223344567",
                        aktørId = "1234567890",
                        LocalDate.now(),
                        "Barn Barnesen",
                        TypeBarn.FRA_OPPSLAG
                    )
                ),
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
                            årsak = FraværÅrsak.SMITTEVERNHENSYN,
                            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                        )
                    )
                )
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        ).valider()
    }
}
