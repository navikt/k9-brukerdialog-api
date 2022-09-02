package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.personopplysninger.Bosteder.BostedPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UtenlandsoppholdTest {

    @Test
    fun `Gyldig Utenlandsopphold gir ingen valideringsfeil`(){
        Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            landkode = "NLD",
            landnavn = "Nederland"
        ).valider("utenlandsopphold").verifiserIngenFeil()
    }

    @Test
    fun `Ugyldig landnavn og landkode gir valideringsfeil`(){
        Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            landkode = "X",
            landnavn = " "
        ).valider("utenlandsopphold").verifiserFeil(2,
            listOf(
                "utenlandsopphold.landkode/landnavn.landnavn kan ikke være tomt eller blankt.",
                "utenlandsopphold.landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }

    @Test
    fun `fraOgMed etter tilOgMed gir valideringsfeil`(){
        Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-04"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            landkode = "NLD",
            landnavn = "Nederland"
        ).valider("utenlandsopphold").verifiserFeil(1,
            listOf(
                "utenlandsopphold.fraOgMed må være før eller lik tilOgMed."
            )
        )
    }

    @Test
    fun `somK9BostedPeriodeInfo fungerer som forventet`(){
        val utenlandsopphold = Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            landkode = "NLD", landnavn = "Nederland"
        )
        val forventet = BostedPeriodeInfo().medLand(Landkode.of("NLD"))
        assertEquals(forventet.land.landkode, utenlandsopphold.somK9BostedPeriodeInfo().land.landkode)
    }

}