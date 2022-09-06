package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsgiver.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.JA
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.NEI
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class ArbeidsgiverTest {

    @Test
    fun `Gyldig arbeidsgiver gir ingen valideringsfeil`(){
        Arbeidsgiver(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI))
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`(){
        Arbeidsgiver(
            navn = " ",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI))
        ).valider().verifiserFeil(1, listOf("arbeidsgiver.navn kan ikke være null eller blankt."))
    }

    @Test
    fun `Ugyldig arbeidsforhold gi valideringsfeil`(){
        Arbeidsgiver(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(JA, emptyList()))
        ).valider().verifiserFeil(1,
            listOf("arbeidsgiver.arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være null/tom når jobberIPerioden=JA.")
        )
    }

    @Test
    fun `Liste med arbeidsgivere med ugyldig organisasjonsnummer skal gi valideringsfeil`(){
        listOf(
            Arbeidsgiver(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "1ABC",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI))
            ),
            Arbeidsgiver(
                navn = "Jakt AS",
                organisasjonsnummer = "CBA1",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI))
            )
        ).valider().verifiserFeil(2, listOf(
            "arbeidsgiver[0].organisasjonsnummer er ikke gyldig.",
            "arbeidsgiver[1].organisasjonsnummer er ikke gyldig."
        ))
    }

    @Test
    fun `Mapping til K9Arbeidstaker blir som forventet`(){
        val k9Arbeidstaker = Arbeidsgiver(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI))
        ).somK9Arbeidstaker(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-10"))
        val forventet = """
            {
              "norskIdentitetsnummer": null,
              "organisasjonsnummer": "991346066",
              "arbeidstidInfo": {
                "perioder": {
                  "2022-01-01/2022-01-10": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  }
                }
              }
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, k9Arbeidstaker.somJson(), true)
    }
}