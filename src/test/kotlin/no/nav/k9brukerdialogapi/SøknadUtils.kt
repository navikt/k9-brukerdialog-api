package no.nav.k9brukerdialogapi

import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.LocalDate
import java.util.UUID

class SøknadUtils {
    companion object{
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("1999-11-02"),
            fornavn = "MOR",
            etternavn = "MORSEN",
            fødselsnummer = "02119970078"
        )

        val metadata = no.nav.k9brukerdialogapi.kafka.Metadata(
            version = 1,
            correlationId = UUID.randomUUID().toString(),
            soknadDialogCommitSha = "abc-123"
        )
    }
}
