package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.helse.TestUtils.Companion.validerFeil
import no.nav.helse.TestUtils.Companion.validerIngenFeil
import kotlin.test.Test

class BekreftelserTest {

    @Test
    fun `Feiler ikke om begge er true`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider("test").validerIngenFeil()
    }

    @Test
    fun `Feiler om man sender harBekreftetOpplysninger som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = true
        ).valider("test").validerFeil(1)
    }

    @Test
    fun `Feiler om man sender harForståttRettigheterOgPlikter som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = false
        ).valider("test").validerFeil(1)
    }
}