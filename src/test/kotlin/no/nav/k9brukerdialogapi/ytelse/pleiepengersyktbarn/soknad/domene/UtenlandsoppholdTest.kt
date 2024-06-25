package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.type.Landkode
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class UtenlandsoppholdTest {

    @Test
    fun `toString har med seg det den skal`() {

        val utenlandsopphold = Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            landkode = "SV",
            erSammenMedBarnet = true,
            landnavn = "Sverige",
            erUtenforEøs = false,
            erBarnetInnlagt = false,
            perioderBarnetErInnlagt = emptyList(),
            årsak = null
        )
        val forventet = utenlandsopphold.toString()
        assertEquals(forventet, "Utenlandsopphold(fraOgMed=2022-01-01, tilOgMed=2022-01-01, landkode='SV', landnavn='Sverige', erUtenforEos=false, erBarnetInnlagt=false, erSammenMedBarnet=true, årsak=null)")
    }
}