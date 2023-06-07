package no.nav.k9brukerdialogapi.utils

import no.nav.k9brukerdialogapi.utils.StringUtils.saniter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SaniterinAvStringsTest {

    @Test
    fun `sanitizing string with all Norwegian characters and special unicode characters`() {
        val input = "Jeg bor på øy’n som heter 'Åyer'. Der er det «mange» \u201Cåpne\u201C ølflasker og ørner. Også mange \u2019bærende\u2019 ærlige mennesker."
        val expected = "Jeg bor på øy'n som heter 'Åyer'. Der er det «mange» \"åpne\" ølflasker og ørner. Også mange 'bærende' ærlige mennesker."

        val actual = saniter(input)

        assertEquals(expected, actual)
    }
}
