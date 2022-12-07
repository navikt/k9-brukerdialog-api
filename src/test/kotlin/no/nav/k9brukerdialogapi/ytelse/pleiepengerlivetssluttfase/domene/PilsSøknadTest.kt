package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class PilsSøknadTest {
    @Test
    fun `Gyldig søknad gir ingen valideringsfeil`(){
        gyldigPILSSøknad().valider().verifiserIngenFeil()
    }

    @Test
    fun `Ugydlig søknad gir valideringsfeil`(){
        assertThrows<Throwblem> {
            gyldigPILSSøknad(
                pleietrengende = Pleietrengende(norskIdentitetsnummer = "ABC", navn = "Bjarne"),
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = null,
                    skalBoIUtlandetNeste12Mnd = true,
                    utenlandsoppholdNeste12Mnd = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.now().plusMonths(3),
                            tilOgMed = LocalDate.now().plusMonths(4),
                            landnavn = "Cuba",
                            landkode = "X"
                        )
                    )
                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(navn = "Org", organisasjonsnummer = "ABC123", erAnsatt = true),
                    Arbeidsgiver(navn = "JobberIkkeHerLenger", organisasjonsnummer = "977155436", erAnsatt = false, sluttetFørSøknadsperiode = false)
                ),
                opptjeningIUtlandet = listOf(
                    OpptjeningIUtlandet(
                        navn = "Kiwi AS",
                        opptjeningType = OpptjeningType.ARBEIDSTAKER,
                        land = Land(landkode = "BEL", landnavn = "Belgia",),
                        fraOgMed = LocalDate.parse("2022-01-10"),
                        tilOgMed = LocalDate.parse("2022-01-09")
                    )
                ),
                utenlandskNæring = listOf(
                    UtenlandskNæring(
                        næringstype = Næringstype.FISKE,
                        navnPåVirksomheten = "Fiskeriet AS",
                        land = Land(landkode = "BEL", landnavn = " ",),
                        fraOgMed = LocalDate.parse("2020-01-01")
                    )
                ),
                utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                    skalOppholdeSegIUtlandetIPerioden = true,
                    opphold = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.parse("2020-02-01"),
                            tilOgMed = LocalDate.parse("2020-02-09"),
                            landnavn = "Cuba",
                            landkode = "UGYLDIG"
                        )
                    )
                ),
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    virksomhet = no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet(
                        fraOgMed = LocalDate.parse("2015-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-01"),
                        næringstype = Næringstype.ANNEN,
                        fiskerErPåBladB = true,
                        navnPåVirksomheten = "Bjarnes Bakeri",
                        registrertINorge = false,
                        registrertIUtlandet = Land("ABW", "Aruba"),
                        næringsinntekt = 9656876,
                        erNyoppstartet = false,
                        harFlereAktiveVirksomheter = false
                    ),
                    arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, emptyList()))
                )
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("medlemskap.harBoddIUtlandetSiste12Mnd kan ikke være null.")}
            assertTrue { it.message.toString().contains("medlemskap.utenlandsoppholdNeste12Mnd[0].landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode.")}
            assertTrue { it.message.toString().contains("arbeidsgivere[0].organisasjonsnummer er ikke gyldig.")}
            assertTrue { it.message.toString().contains("pleietrengende.norskIdentitetsnummer er ikke gyldig identifikator, 'ABC*****'. Forventet at personidentifikator kun var siffer, men var ABC****** (3)")}
            assertTrue { it.message.toString().contains("utenlandskNæring[0].land.landnavn kan ikke være tomt eller blankt.")}
            assertTrue { it.message.toString().contains("opptjeningIUtlandet[0].tilOgMed må være lik eller etter fraOgMed.")}
            assertTrue { it.message.toString().contains("utenlandsoppholdIPerioden.opphold[0].landkode/landnavn.landkode 'UGYLDIG' er ikke en gyldig ISO 3166-1 alpha-3 kode.")}
            assertTrue { it.message.toString().contains("selvstendigNæringsdrivende.arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=REDUSERT.")}
        }
    }

    @Test
    fun `Gyldig søknad mappes til forventet K9Format`(){
        //language=json
        val forventet = """
            {
              "søknadId": "4e62f8de-1ff6-40e9-bdcd-10485c789094",
              "versjon": "1.0.0",
              "mottattDato": "2022-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": {
                "type": "PLEIEPENGER_LIVETS_SLUTTFASE",
                "pleietrengende": {
                  "norskIdentitetsnummer": "06098523047",
                  "fødselsdato": null
                },
                "søknadsperiode": [
                  "2021-01-01/2021-01-10"
                ],
                "trekkKravPerioder": [],
                "opptjeningAktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2015-01-01/2021-01-01": {
                          "virksomhetstyper": [
                            "ANNEN"
                          ],
                          "bruttoInntekt": 9656876,
                          "erNyoppstartet": false,
                          "registrertIUtlandet": true,
                          "landkode": "CUB"
                        }
                      },
                      "virksomhetNavn": "Bjarnes Bakeri"
                    }
                  ],
                  "frilanser": {
                    "startdato": "2019-01-01",
                    "sluttdato": "2021-05-01"
                  }
                },
                "bosteder": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "land": "BRA"
                    },
                    "2023-01-01/2023-01-10": {
                      "land": "CUB"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2020-02-01/2020-02-09": {
                      "land": "CUB",
                      "årsak": null
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "lovbestemtFerie": {
                  "perioder": {}
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "917755736",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-01": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT4H"
                          },
                          "2021-01-04/2021-01-04": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-05/2021-01-05": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-06/2021-01-06": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-07/2021-01-07": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-08/2021-01-08": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          }
                        }
                      }
                    },
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "977155436",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-10": {
                            "jobberNormaltTimerPerDag": "PT0S",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          }
                        }
                      }
                    }
                  ],
                  "frilanserArbeidstidInfo": {
                    "perioder": {
                      "2021-01-01/2021-01-10": {
                        "jobberNormaltTimerPerDag": "PT0S",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      }
                    }
                  },
                  "selvstendigNæringsdrivendeArbeidstidInfo": {
                    "perioder": {
                      "2021-01-01/2021-01-10": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      }
                    }
                  }
                },
                "uttak": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "timerPleieAvBarnetPerDag": "PT7H30M"
                    }
                  }
                }
              },
              "språk": "nb",
              "journalposter": [],
              "begrunnelseForInnsending": {
                "tekst": null
              }
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, gyldigPILSSøknad().somK9Format(søker).somJson(), true)
    }
}
