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
        ).valider("bekreftelser").verifiserIngenFeil()
    }

    @Test
    fun `Feiler om man sender harBekreftetOpplysninger som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = true
        ).valider("bekreftelser").verifiserFeil(1, listOf("bekreftelser.harBekreftetOpplysninger må være true"))
    }

    @Test
    fun `Feiler om man sender harForståttRettigheterOgPlikter som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = false
        ).valider("bekreftelser").verifiserFeil(1, listOf("bekreftelser.harForståttRettigheterOgPlikter må være true"))
    }
}