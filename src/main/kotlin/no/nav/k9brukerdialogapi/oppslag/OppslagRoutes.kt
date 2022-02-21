package no.nav.k9brukerdialogapi.oppslag

import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.OPPSLAG_URL
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.barn.barnApis
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.oppslag.søker.søkerApis

fun Route.oppslagRoutes(
    idTokenProvider: IdTokenProvider,
    søkerService: SøkerService,
    barnservice: BarnService
){
    route(OPPSLAG_URL){
        søkerApis(søkerService, idTokenProvider)
        barnApis(barnservice, idTokenProvider)
    }
}