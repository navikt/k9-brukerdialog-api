package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Land
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Næringstype.FISKE
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.UtenlandskNæring.Companion.valider
import java.time.LocalDate
import kotlin.test.Test

class UtenlandskNæringTest{

    @Test
    fun `Gyldig UtenlandskNæring gir ingen valideringsfeil`(){
        UtenlandskNæring(
            næringstype = FISKE,
            navnPåVirksomheten = "Fisker'n",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = null
        ).valider("utenlandskNæring").verifiserIngenFeil()
    }

    @Test
    fun `UtenlandskNæring med ugyldig land gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = FISKE,
            navnPåVirksomheten = "Fisker'n",
            land = Land("MARS", "  "),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = null
        ).valider("utenlandskNæring").verifiserFeil(2,
            listOf(
                "utenlandskNæring.land.Landkode 'MARS' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring.land.landnavn kan ikke være tomt eller blankt."
            )
        )
    }

    @Test
    fun `Liste med UtenlandskNæring hvor fraOgMed er etter tilOgMed gir valideringsfeil`() {
        listOf(
            UtenlandskNæring(
                FISKE, "Fisker'n",
                Land("NLD", "Nederland"), "123ABC",
                LocalDate.parse("2022-01-10"), LocalDate.parse("2022-01-01")
            ),
            UtenlandskNæring(
                FISKE, "Fisker'n",
                Land("NLD", "Nederland"), "123ABC",
                LocalDate.parse("2022-01-10"), LocalDate.parse("2022-01-01")
            )
        ).valider("utenlandskNæring").verifiserFeil(2,
            listOf(
                "utenlandskNæring[0].tilOgMed må være lik eller etter fraOgMed.",
                "utenlandskNæring[1].tilOgMed må være lik eller etter fraOgMed."
            )
        )
    }
}