package no.nav.k9brukerdialogapi.oppslag

import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.OPPSLAG_URL
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.oppslag.søker.søkerApis

fun Route.oppslagRoute(
    idTokenProvider: IdTokenProvider,
    søkerService: SøkerService
){
    route(OPPSLAG_URL){
        søkerApis(søkerService, idTokenProvider)
    }
}