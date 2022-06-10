package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsperiode
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class OmsorgspengerUtbetalingSnfSøknadTest {

    @Test
    fun `Skal gi valideringsfeil dersom alle barna er over 13 år men ingen har utvidet rett`() {
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                barn = listOf(
                    Barn(
                        navn = "Barnesen",
                        fødselsdato = LocalDate.now().minusYears(14),
                        type = TypeBarn.FRA_OPPSLAG,
                        utvidetRett = false,
                        identitetsnummer = "26104500284"
                    ),
                    Barn(
                        navn = "Barnesen",
                        fødselsdato = LocalDate.now().minusYears(14),
                        type = TypeBarn.FRA_OPPSLAG,
                        utvidetRett = false,
                        identitetsnummer = "111111"
                    )
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("Hvis alle barna er 13 år eller eldre må minst et barn ha utvidet rett.") }
        }
    }

    @Test
    fun `Gyldig søknad blir mappet til forventet k9Format`() {
        val søknad = Søknad(
            språk = "nb",
            mottatt = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
            bosteder = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "NLD",
                    landnavn = "Nederland",
                    erEØSLand = true
                )
            ),
            opphold = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2022-02-01"),
                    tilOgMed = LocalDate.parse("2022-02-10"),
                    landkode = "BE",
                    landnavn = "Belgia",
                    erEØSLand = true
                )
            ),
            spørsmål = listOf(
                SpørsmålOgSvar("Har du hund?", true)
            ),
            harDekketTiFørsteDagerSelv = null,
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ),
            utbetalingsperioder = listOf(
                Utbetalingsperiode(
                    fraOgMed = LocalDate.parse("2022-01-20"),
                    tilOgMed = LocalDate.parse("2022-01-25"),
                    antallTimerBorte = Duration.ofHours(5),
                    antallTimerPlanlagt = Duration.ofHours(7),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.FRILANSER)
                ),
                Utbetalingsperiode(
                    fraOgMed = LocalDate.parse("2022-01-20"),
                    tilOgMed = LocalDate.parse("2022-01-25"),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
                )
            ),
            andreUtbetalinger = listOf(AndreUtbetalinger.DAGPENGER),
            erArbeidstakerOgså = false,
            barn = listOf(
                Barn(
                    navn = "Barnesen",
                    fødselsdato = LocalDate.parse("2022-01-01"),
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = null,
                    utvidetRett = null,
                    identitetsnummer = "26104500284"
                )
            ),
            frilans = Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = LocalDate.parse("2022-10-01"),
                jobberFortsattSomFrilans = true
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstyper = listOf(Næringstyper.DAGMAMMA, Næringstyper.JORDBRUK_SKOGBRUK),
                næringsinntekt = 3_000_000,
                navnPåVirksomheten = "Kiwi ASA",
                organisasjonsnummer = "975959171",
                registrertINorge = false,
                registrertIUtlandet = Land("BEL", "Belgia"),
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                    oppstartsdato = LocalDate.parse("2022-01-01")
                ),
                varigEndring = VarigEndring(
                    dato = LocalDate.parse("2022-01-01"),
                    inntektEtterEndring = 1_500_00,
                    forklaring = "Fordi atte atte atte"
                ),
                regnskapsfører = Regnskapsfører(
                    navn = "Knut",
                    telefon = "123123123"
                ),
                erNyoppstartet = true,
                harFlereAktiveVirksomheter = true
            ),
            vedlegg = listOf()
        )
        val k9Format = søknad.somK9Format(SøknadUtils.søker).somJson()
        val forventetK9Format = """
            {
              "søknadId": "${søknad.søknadId.id}",
              "versjon": "1.0.0",
              "mottattDato": "2022-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": {
                "type": "OMP_UT",
                "fosterbarn": [
                  {
                    "norskIdentitetsnummer": "26104500284",
                    "fødselsdato": null
                  }
                ],
                "aktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2022-01-01/2022-10-01": {
                          "virksomhetstyper": [
                            "DAGMAMMA",
                            "JORDBRUK_SKOGBRUK"
                          ],
                          "regnskapsførerNavn": "Knut",
                          "regnskapsførerTlf": "123123123",
                          "erVarigEndring": true,
                          "erNyIArbeidslivet": true,
                          "endringDato": "2022-01-01",
                          "endringBegrunnelse": "Fordi atte atte atte",
                          "bruttoInntekt": 150000,
                          "erNyoppstartet": true,
                          "registrertIUtlandet": true,
                          "landkode": "BEL"
                        }
                      },
                      "organisasjonsnummer": "975959171",
                      "virksomhetNavn": "Kiwi ASA"
                    }
                  ],
                  "frilanser": {
                    "startdato": "2022-01-01",
                    "sluttdato": "2022-10-01"
                  }
                },
                "fraværsperioder": [
                  {
                    "periode": "2022-01-20/2022-01-25",
                    "duration": "PT5H",
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": null,
                    "aktivitetFravær": [
                      "FRILANSER"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": null
                  },
                  {
                    "periode": "2022-01-20/2022-01-25",
                    "duration": null,
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": null,
                    "aktivitetFravær": [
                      "SELVSTENDIG_VIRKSOMHET"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": null
                  }
                ],
                "fraværsperioderKorrigeringIm": null,
                "bosteder": {
                  "perioder": {
                    "2022-01-01/2022-01-10": {
                      "land": "NLD"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2022-02-01/2022-02-10": {
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
        JSONAssert.assertEquals(forventetK9Format, k9Format, true)
    }
}