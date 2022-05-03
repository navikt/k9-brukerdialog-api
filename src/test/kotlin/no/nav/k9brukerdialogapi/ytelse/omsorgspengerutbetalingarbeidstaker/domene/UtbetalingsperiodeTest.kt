package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import org.junit.jupiter.api.assertThrows
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
            antallTimerPlanlagt = Duration.ofHours(7)
        )
    }

    @Test
    fun `Utbetalingsperiode hvor fraOgMed er etter tilOgMed gir feil`() {
        assertThrows<IllegalArgumentException> {
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().minusDays(4),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7)
            )
        }
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerBorte er mer enn antallTimerPlanlagt gir feil`() {
        assertThrows<IllegalArgumentException> {
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerBorte = Duration.ofHours(7),
                antallTimerPlanlagt = Duration.ofHours(5)
            )
        }
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerPlanlagt er satt men antallTimerBorte er null gir feil`() {
        assertThrows<IllegalArgumentException> {
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = null
            )
        }
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerBorte er satt men antallTimerPlanlagt er null gir feil`() {
        assertThrows<IllegalArgumentException> {
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = null
            )
        }
    }
}