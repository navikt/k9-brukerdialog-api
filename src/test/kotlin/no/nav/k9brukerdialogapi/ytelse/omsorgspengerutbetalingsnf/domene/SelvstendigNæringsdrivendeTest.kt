package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Næringstyper.DAGMAMMA
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class SelvstendigNæringsdrivendeTest {

    @Test
    fun `SelvstendigNæringsdrivende blir mappet til riktig K9SelvstendigNæringsdrivende`(){
        val k9SelvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-10-01"),
            næringstype = DAGMAMMA,
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
        ).somK9SelvstendigNæringsdrivende().somJson()

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
                  "registrertIUtlandet": true,
                  "landkode": "BEL"
                }
              },
              "organisasjonsnummer": "975959171",
              "virksomhetNavn": "Kiwi ASA"
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventet), JSONObject(k9SelvstendigNæringsdrivende), true)
    }

    @Test
    fun `Gyldig sn gir ingen valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-10-01"),
            næringstype = DAGMAMMA,
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
        ).valider("sn").verifiserIngenFeil()
    }

    @Test
    fun `Sn med ugyldig organisasjonsnummer gir valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-10-01"),
            næringstype = DAGMAMMA,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "123ABC",
            registrertINorge = true,
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = false
        ).valider("sn")
            .verifiserFeil(1, listOf("sn.organisasjonsnummer kan kun bestå av tall."))
    }

    @Test
    fun `Sn med ugyldig registrertIUtlandet gir valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-10-01"),
            næringstype = DAGMAMMA,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertIUtlandet = Land("ABC", "Nederland"),
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = false
        ).valider("sn")
            .verifiserFeil(1, listOf("sn.registrertIUtlandet.Landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode."))
    }

    @Test
    fun `Sn med fraOgMed etter tilOgMed gir valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2021-01-01"),
            næringstype = DAGMAMMA,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertINorge = true,
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = false
        ).valider("sn")
            .verifiserFeil(1, listOf("sn.tilOgMed må være før eller lik tilOgMed."))
    }

    @Test
    fun `Sn med harFlereAktiveVirksomheter som null gir valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            næringstype = DAGMAMMA,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertINorge = true,
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = null
        ).valider("sn")
            .verifiserFeil(1, listOf("sn.harFlereAktiveVirksomheter kan ikke være null."))
    }

    @Test
    fun `Sn med erNyoppstartet=true men fraOgMed som er over 4 år gammel gir valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2015-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            næringstype = DAGMAMMA,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertINorge = true,
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = false
        ).valider("sn")
            .verifiserFeil(1, listOf("sn.fraOgMed Dersom nyOppstartet er true må fraOgMed være maks 4 år siden."))
    }

    @Test
    fun `Sn med erNyoppstartet=false men fraOgMed som er under 4 år gammel gir valideringsfeil`(){
        SelvstendigNæringsdrivende(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            næringstype = DAGMAMMA,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertINorge = true,
            erNyoppstartet = false,
            harFlereAktiveVirksomheter = false
        ).valider("sn")
            .verifiserFeil(1, listOf("sn.fraOgMed Dersom nyOppstartet er false må fraOgMed være over 4 år siden."))
    }
}