package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class BekreftelserTest {

    @Test
    fun `Feiler ikke om begge er true`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = false
        )
    }

    @Test
    fun `Feiler om man sender harBekreftetOpplysninger som null`() {
        assertThrows<IllegalArgumentException> {
            Bekreftelser(
                harBekreftetOpplysninger = null,
                harForståttRettigheterOgPlikter = true
            )
        }
    }

    @Test
    fun `Feiler om man sender harForståttRettigheterOgPlikter som null`() {
        assertThrows<IllegalArgumentException> {
            Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = null
            )
        }
    }
}