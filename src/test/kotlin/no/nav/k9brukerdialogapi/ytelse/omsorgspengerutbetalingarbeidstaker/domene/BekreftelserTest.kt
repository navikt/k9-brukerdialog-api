package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import kotlin.test.Test

class BekreftelserTest {

    @Test
    fun `Feiler ikke om begge er true`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider("test").verifiserIngenFeil()
    }

    @Test
    fun `Feiler om man sender harBekreftetOpplysninger som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = true
        ).valider("test").verifiserFeil(1)
    }

    @Test
    fun `Feiler om man sender harForståttRettigheterOgPlikter som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = false
        ).valider("test").verifiserFeil(1)
    }
}