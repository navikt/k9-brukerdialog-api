package no.nav.k9brukerdialogapi.oppslag

import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.FraOgMedTilOgMedValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FraOgMedTilOgMedValidatorTest {

    @Test
    fun `tilOgMed kan ikke være før fraOgMed`(){
        val feil = FraOgMedTilOgMedValidator.valider(
            fraOgMed = "2021-01-05",
            tilOgMed = "2021-01-04"
        )

        val forventetFeilReason = "til_og_med kan ikke være før fra_og_med"
        assertTrue(feil.isNotEmpty())
        assertEquals(forventetFeilReason, feil.first().reason)
    }

    @Test
    fun `tilOgMed kan ikke være null`(){
        val feil = FraOgMedTilOgMedValidator.valider(
            fraOgMed = "2021-01-05",
            tilOgMed = null
        )

        val forventetFeilReason = "til_og_med er på ugyldig format. Feil: java.lang.NullPointerException: text"
        assertTrue(feil.isNotEmpty())
        assertEquals(forventetFeilReason, feil.first().reason)
    }

    @Test
    fun `fraOgMed kan ikke være null`(){
        val feil = FraOgMedTilOgMedValidator.valider(
            tilOgMed = "2021-01-05",
            fraOgMed = null
        )

        val forventetFeilReason = "fra_og_med er på ugyldig format. Feil: java.lang.NullPointerException: text"
        assertTrue(feil.isNotEmpty())
        assertEquals(forventetFeilReason, feil.first().reason)
    }
}