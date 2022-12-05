package no.nav.k9brukerdialogapi.innsending

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.test.assertEquals

internal class InnsendingCacheTest {

    @Test
    fun `gitt en eksisterende nøkkel eksisterer, forvent at det feiler med Throblem`() {
        val innsendingCache = InnsendingCache(1)
        innsendingCache.put("123456789_${Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE}")

        val problemDetails = assertThrows<Throwblem> {
            innsendingCache.put("123456789_${Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE}")
        }.getProblemDetails()

        assertEquals("Duplikat innsending", problemDetails.title)
        assertEquals(URI("/problem-details/duplikat-innsendin"), problemDetails.type)
        assertEquals(400, problemDetails.status)
        assertEquals("Det ble funnet en eksisterende innsending på søker med samme ytelse.", problemDetails.detail)
        assertEquals(URI(""), problemDetails.instance)
    }
}
