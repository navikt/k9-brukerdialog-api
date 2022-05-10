package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogapi.general.validerFeil
import no.nav.k9brukerdialogapi.general.validerIngenFeil
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