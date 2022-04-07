package no.nav.k9brukerdialogapi

import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.LocalDate

class SøknadUtils {
    companion object{
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fornavn = "Kjell",
            fødselsnummer = "26104500284"
        )
    }
}