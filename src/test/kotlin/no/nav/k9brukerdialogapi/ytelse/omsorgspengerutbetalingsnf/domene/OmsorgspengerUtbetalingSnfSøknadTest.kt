package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.VarigEndring
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
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
                        fødselsdato = LocalDate.now().minusYears(13).minusDays(1),
                        type = TypeBarn.FRA_OPPSLAG,
                        utvidetRett = false,
                        identitetsnummer = "11880898304"
                    )
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("Hvis alle barna er 13 år eller eldre må minst et barn ha utvidet rett.") }
        }
    }

    @Test
    fun `Skal ikke gi valideringsfeil dersom alle barna er over 13 år og minst et barn har utvidet rett`() {
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
                    fødselsdato = LocalDate.now().minusYears(13).minusDays(1),
                    type = TypeBarn.FRA_OPPSLAG,
                    utvidetRett = true,
                    identitetsnummer = "11880898304"
                )
            )
        ).valider()
    }

    @Test
    fun `Skal gi valideringsfeil dersom et barna er 12 år men harDekketTiFørsteDagerSelv er false`() {
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                barn = listOf(
                    Barn(
                        navn = "Barnesen",
                        fødselsdato = LocalDate.now().minusYears(12),
                        type = TypeBarn.FRA_OPPSLAG,
                        utvidetRett = false,
                        identitetsnummer = "26104500284"
                    )
                ),
                harDekketTiFørsteDagerSelv = false
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("Dersom et barn er 12 år eller yngre må harDekketTiFørsteDagerSelv være true.") }
        }
    }

    @Test
    fun `Skal ikke gi valideringsfeil dersom et barna er 12 år og harDekketTiFørsteDagerSelv er true`() {
        genererSøknadForOmsUtSnf(
            barn = listOf(
                Barn(
                    navn = "Barnesen",
                    fødselsdato = LocalDate.now().minusYears(12),
                    type = TypeBarn.FRA_OPPSLAG,
                    identitetsnummer = "26104500284"
                )
            ),
            harDekketTiFørsteDagerSelv = true
        ).valider()
    }

    @Test
    fun `Ugyldig opphold og bosteder skal gi validerinsfeil`(){
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                bosteder = listOf(
                    Bosted(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(2),
                        landkode = "BEL",
                        landnavn = "Belgia",
                        erEØSLand = null
                    )
                ),
                opphold = listOf(
                    Opphold(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(2),
                        landkode = "BEL",
                        landnavn = " ",
                        erEØSLand = true
                    )
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("bosteder[0].erEØSLand må være satt") }
            assertTrue { it.message.toString().contains("opphold[0].landnavn kan ikke være blankt eller tomt. landnavn=' '") }
        }
    }

    @Test
    fun `Ugyldig utbetalingsperioder skal gi valideringsfeil`(){
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                utbetalingsperiode = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-20"),
                        tilOgMed = LocalDate.parse("2022-01-19"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
                    ),
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-20"),
                        tilOgMed = LocalDate.parse("2022-01-24"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf()
                    )
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("utbetalingsperioder[0].tilOgMed må være lik eller etter fraOgMed.") }
            assertTrue { it.message.toString().contains("utbetalingsperioder[1].aktivitetFravær kan ikke være tom.") }
        }
    }

    @Test
    fun `Ugyldig bekreftelser skal gi valideringsfeil`(){
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                bekreftelser = Bekreftelser(
                    harBekreftetOpplysninger = false,
                    harForståttRettigheterOgPlikter = null
                ),
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("bekreftelser.harBekreftetOpplysninger må være true") }
            assertTrue { it.message.toString().contains("bekreftelser.harForståttRettigheterOgPlikter må være true") }
        }
    }

    @Test
    fun `Ugyldig barn skal gi valideringsfeil`(){
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                barn = listOf(
                    Barn(
                        navn = "Barnesen",
                        fødselsdato = LocalDate.now().minusYears(14),
                        type = TypeBarn.FRA_OPPSLAG,
                        utvidetRett = true,
                        identitetsnummer = "123"
                    )
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("barn[0].identitetsnummer er ikke gyldig identifikator, '123*****'") }
        }
    }

    @Test
    fun `Ugyldig frilans skal gi valideringsfeil`(){
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                frilans = Frilans(
                    startdato = LocalDate.parse("2022-01-01"),
                    sluttdato = LocalDate.parse("2022-10-01"),
                    jobberFortsattSomFrilans = null
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("frilans.jobberFortsattSomFrilans kan ikke være null") }
        }
    }

    @Test
    fun `Ugyldig selvstendigNæringsdrivende skal gi valideringsfeil`() {
        assertThrows<Throwblem> {
            genererSøknadForOmsUtSnf(
                selvstendigNæringsdrivende = Virksomhet(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-10-01"),
                    næringstype = Næringstype.DAGMAMMA,
                    navnPåVirksomheten = "Kiwi ASA",
                    organisasjonsnummer = "123ABC",
                    erNyoppstartet = true,
                    harFlereAktiveVirksomheter = false
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("selvstendigNæringsdrivende.organisasjonsnummer kan kun bestå av tall.") }
        }
    }

    @Test
    fun `Gyldig søknad blir mappet til forventet k9Format`() {
        val søknad = OmsorgspengerutbetalingSnfSøknad(
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
            erArbeidstakerOgså = false,
            barn = listOf(
                Barn(
                    navn = "Barnesen",
                    fødselsdato = LocalDate.parse("2022-01-01"),
                    type = TypeBarn.FOSTERBARN,
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
            selvstendigNæringsdrivende = Virksomhet(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
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
            vedlegg = listOf(),
            dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
        )
        val k9Format = søknad.somK9Format(SøknadUtils.søker, metadata).somJson()
        val forventetK9Format = """
            {
              "søknadId": "${søknad.søknadId.id}",
              "versjon": "1.1.0",
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
                    "duration": "PT5H30M",
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": null,
                    "aktivitetFravær": [
                      "FRILANSER"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": null,
                    "delvisFravær": {
                      "normalarbeidstid":"PT7H",
                      "fravær":"PT5H"
                    }
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
                    "arbeidsgiverOrgNr": null,
                    "delvisFravær": null
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
        JSONAssert.assertEquals(forventetK9Format, k9Format, true)
    }
}
