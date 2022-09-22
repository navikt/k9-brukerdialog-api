package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.VarigEndring
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class SelvstendigNæringsdrivendeTest {

    @Test
    fun `Mapping til k9Format blir som forventet`(){
        val k9SelvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            virksomhet = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = Næringstype.DAGMAMMA,
                næringsinntekt = 3_000_000,
                navnPåVirksomheten = "Kiwi ASA",
                organisasjonsnummer = "975959171",
                registrertINorge = true,
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
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, emptyList()))
        ).somK9SelvstendigNæringsdrivende()

        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-10-01": {
                  "virksomhetstyper": [
                    "DAGMAMMA"
                  ],
                  "regnskapsførerNavn": "Knut",
                  "regnskapsførerTlf": "123123123",
                  "erVarigEndring": true,
                  "erNyIArbeidslivet": true,
                  "endringDato": "2022-01-01",
                  "endringBegrunnelse": "Fordi atte atte atte",
                  "bruttoInntekt": 150000,
                  "erNyoppstartet": true,
                  "registrertIUtlandet": false,
                  "landkode": "NOR"
                }
              },
              "organisasjonsnummer": "975959171",
              "virksomhetNavn": "Kiwi ASA"
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventet), JSONObject(k9SelvstendigNæringsdrivende.somJson()), true)
    }

    @Test
    fun `Mapping til K9Arbeidstid blir som forventet`(){
        val mandag = LocalDate.parse("2022-08-01")
        val fredag = mandag.plusDays(5)
        val arbeidstidInfo = SelvstendigNæringsdrivende(
            virksomhet = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = Næringstype.DAGMAMMA,
                navnPåVirksomheten = "Kiwi ASA",
                erNyoppstartet = true,
            ),
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, emptyList()))
        ).somK9ArbeidstidInfo(mandag, fredag)
        val forventet = """
            {
              "perioder": {
                "2022-08-01/2022-08-06": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventet), JSONObject(arbeidstidInfo.somJson()), true)
    }

    @Test
    fun `Gyldig SelvstendigNæringsdrivende gir ingen valideringsfeil`(){
        SelvstendigNæringsdrivende(
            virksomhet = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = Næringstype.DAGMAMMA,
                næringsinntekt = 3_000_000,
                navnPåVirksomheten = "Kiwi ASA",
                organisasjonsnummer = "975959171",
                registrertINorge = true,
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
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, emptyList()))
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `SelvstendigNæringsdrivende med feil i virksomhet og arbeidsforhold skal gi valideringsfeil`(){
        SelvstendigNæringsdrivende(
            virksomhet = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = Næringstype.DAGMAMMA,
                næringsinntekt = 3_000_000,
                navnPåVirksomheten = "Kiwi ASA",
                organisasjonsnummer = "123ABC",
                registrertINorge = true,
                regnskapsfører = Regnskapsfører(
                    navn = "Knut",
                    telefon = "123123123"
                ),
                erNyoppstartet = true,
                harFlereAktiveVirksomheter = true
            ),
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, emptyList()))
        ).valider().verifiserFeil(2, listOf(
            "selvstendigNæringsdrivende.virksomhet.organisasjonsnummer kan kun bestå av tall.",
            "selvstendigNæringsdrivende.arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=JA."
        ))
    }
}