package no.nav.k9brukerdialogapi.oppslag.søker

import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.oppslag.søker.Søker

internal fun Søker.valider() {
    if (!myndig) {
        throw Throwblem(DefaultProblemDetails(
            title = "unauthorized",
            status = 403,
            detail = "Søkeren er ikke myndig og kan ikke sende inn søknaden."
        ))
    }
}
