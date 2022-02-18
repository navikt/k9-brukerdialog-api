package no.nav.k9brukerdialogapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.LocalDate

internal object EttersendingUtils {
    internal val objectMapper = jacksonObjectMapper().k9BrukerdialogKonfiguert()

    val søker = Søker(
        aktørId = "12345",
        fødselsdato = LocalDate.parse("2000-01-01"),
        fødselsnummer = "02119970078",
        fornavn = "Ole",
        mellomnavn = "Dole",
        etternavn = "Doffen"
    )
}

fun Any.somJson() = objectMapper.writeValueAsString(this)