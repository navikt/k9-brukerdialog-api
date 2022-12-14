package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandskNæring
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandskNæring.Companion.valider
import java.time.LocalDate
import kotlin.test.Test

class UtenlandskNæringTest {

    @Test
    fun `Gyldig utenlandskNæring gir ingen valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("utenlandskNæring[0]").verifiserIngenFeil()
    }

    @Test
    fun `UtenlandskNæring med ugyldig land gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("ABC", " "),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("utenlandskNæring[0]").verifiserFeil(2,
            listOf(
                "utenlandskNæring[0].land.landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring[0].land.landnavn kan ikke være tomt eller blankt."
            )
        )
    }

    @Test
    fun `UtenlandskNæring med tilOgMed før fraOgMed gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-05"),
            tilOgMed = LocalDate.parse("2022-01-01")
        ).valider("utenlandskNæring[0]").verifiserFeil(1,
            listOf("utenlandskNæring[0].tilOgMed må være lik eller etter fraOgMed")
        )
    }

    @Test
    fun `Liste med UtenlandskNæring som har feil i land gir valideringsfeil`(){
        listOf(
            UtenlandskNæring(
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "Flush AS",
                land = Land("CBA", "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-03")
            ),
            UtenlandskNæring(
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "Flush AS",
                land = Land("ABC", "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-05")
            )
        ).valider("utenlandskNæring").verifiserFeil(2,
            listOf(
                "utenlandskNæring[0].land.landkode 'CBA' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring[1].land.landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }
}
