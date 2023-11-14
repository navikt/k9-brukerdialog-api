package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsgiver.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.SOM_VANLIG
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.INGEN_ARBEIDSDAG
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
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(SOM_VANLIG, PILSTestUtils.enkeltDagerMedJobbSomVanlig))
        ).valider().verifiserIngenFeil()
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`(){
        Arbeidsgiver(
            navn = " ",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, PILSTestUtils.enkeltDagerMedFulltFravær))
        ).valider().verifiserFeil(1, listOf("arbeidsgiver.navn kan ikke være null eller blankt."))
    }

    @Test
    fun `Ugyldig arbeidsforhold gi valideringsfeil`(){
        Arbeidsgiver(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, emptyList()))
        ).valider().verifiserFeil(1,
            listOf("arbeidsgiver.arbeidsforhold.arbeidIPeriode.enkeltdager kan ikke være tom liste.")
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
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, PILSTestUtils.enkeltDagerMedFulltFravær))
            ),
            Arbeidsgiver(
                navn = "Jakt AS",
                organisasjonsnummer = "CBA1",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, PILSTestUtils.enkeltDagerMedFulltFravær))
            )
        ).valider().verifiserFeil(2, listOf(
            "arbeidsgivere[0].organisasjonsnummer er ikke gyldig.",
            "arbeidsgivere[1].organisasjonsnummer er ikke gyldig."
        ))
    }

    @Test
    fun `Mapping til K9Arbeidstaker blir som forventet`(){
        val k9Arbeidstaker = Arbeidsgiver(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, listOf(
                Enkeltdag(LocalDate.parse("2022-01-01"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-02"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-03"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-04"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-05"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-06"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-07"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-08"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-09"), INGEN_ARBEIDSDAG),
                Enkeltdag(LocalDate.parse("2022-01-10"), INGEN_ARBEIDSDAG),
            )))
        ).somK9Arbeidstaker(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-10"))
        val forventet = """
            {
              "norskIdentitetsnummer": null,
              "organisasjonsnummer": "991346066",
              "organisasjonsnavn": "Fiskeriet AS",
              "arbeidstidInfo": {
                "perioder": {
                  "2022-01-01/2022-01-01": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-02/2022-01-02": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-03/2022-01-03": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-04/2022-01-04": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-05/2022-01-05": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-06/2022-01-06": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-07/2022-01-07": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-08/2022-01-08": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-09/2022-01-09": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-10/2022-01-10": {
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
