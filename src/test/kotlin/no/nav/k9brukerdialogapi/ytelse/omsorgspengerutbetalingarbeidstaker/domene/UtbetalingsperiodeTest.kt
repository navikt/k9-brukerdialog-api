package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.helse.TestUtils.Companion.validerFeil
import no.nav.helse.TestUtils.Companion.validerIngenFeil
import no.nav.k9.søknad.felles.fravær.AktivitetFravær
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.FraværÅrsak.ORDINÆRT_FRAVÆR
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test

class UtbetalingsperiodeTest {

    @Test
    fun `Gyldig utbetalingsperiode gir ingen feil`() {
        Utbetalingsperiode(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(4),
            antallTimerBorte = Duration.ofHours(5),
            antallTimerPlanlagt = Duration.ofHours(7),
            årsak = ORDINÆRT_FRAVÆR
        ).valider("test").validerIngenFeil()
    }

    @Test
    fun `Utbetalingsperiode hvor fraOgMed er etter tilOgMed gir feil`() {
        Utbetalingsperiode(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(4),
            antallTimerBorte = Duration.ofHours(5),
            antallTimerPlanlagt = Duration.ofHours(7),
            årsak = ORDINÆRT_FRAVÆR
        ).valider("test").validerFeil(1)
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerBorte er mer enn antallTimerPlanlagt gir feil`() {
        Utbetalingsperiode(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(4),
            antallTimerBorte = Duration.ofHours(7),
            antallTimerPlanlagt = Duration.ofHours(5),
            årsak = ORDINÆRT_FRAVÆR
        ).valider("test").validerFeil(1)
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerPlanlagt er satt men antallTimerBorte er null gir feil`() {
        Utbetalingsperiode(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(4),
            antallTimerPlanlagt = Duration.ofHours(5),
            antallTimerBorte = null,
            årsak = ORDINÆRT_FRAVÆR
        ).valider("test").validerFeil(1)
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerBorte er satt men antallTimerPlanlagt er null gir feil`() {
        Utbetalingsperiode(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(4),
            antallTimerBorte = Duration.ofHours(5),
            antallTimerPlanlagt = null,
            årsak = ORDINÆRT_FRAVÆR
        ).valider("test").validerFeil(1)
    }

    @Test
    fun `Genererer forventet FraværPeriode`() {
        val utbetalingsperiode = Utbetalingsperiode(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            antallTimerBorte = Duration.ofHours(5),
            antallTimerPlanlagt = Duration.ofHours(7),
            årsak = ORDINÆRT_FRAVÆR
        )
        val faktiskFraværPeriode = utbetalingsperiode.somFraværPeriode(
            SøknadÅrsak.ARBEIDSGIVER_KONKURS,
            listOf(AktivitetFravær.ARBEIDSTAKER),
            Organisasjonsnummer.of("825905162")
        ).somJson()
        val forventetFraværPeriode = """
            {
              "periode": "2022-01-01/2022-01-10",
              "duration": "PT5H",
              "årsak": "ORDINÆRT_FRAVÆR",
              "søknadÅrsak": "ARBEIDSGIVER_KONKURS",
              "aktivitetFravær": [
                "ARBEIDSTAKER"
              ],
              "arbeidsforholdId": null,
              "arbeidsgiverOrgNr": "825905162"
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetFraværPeriode, faktiskFraværPeriode, true)
    }
}