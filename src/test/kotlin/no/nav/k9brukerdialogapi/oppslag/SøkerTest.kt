package no.nav.k9brukerdialogapi.oppslag

import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SøkerTest {

    @Test
    fun `En person som fyller 18 i morgen kan ikke søke`() {
        val soker = Søker(
            aktørId = "1234",
            fødselsnummer = "29099012345",
            fødselsdato = LocalDate.now().minusYears(18).plusDays(1)
        )
        assertFalse(soker.erMyndig())
    }

    @Test
    fun `En person som fyller 18 i dag kan søke`() {
        val soker = Søker(
            aktørId = "5678",
            fødselsnummer = "29099012345",
            fødselsdato = LocalDate.now().minusYears(18)
        )
        assertTrue(soker.erMyndig())
    }
}