package no.nav.k9brukerdialogapi.oppslag

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.OPPSLAG_URL
import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.ArbeidsgiverService
import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.arbeidsgiverApis
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.barn.barnApis
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.oppslag.søker.søkerApis

fun Route.oppslagRoutes(
    idTokenProvider: IdTokenProvider,
    søkerService: SøkerService,
    barnservice: BarnService,
    arbeidsgiverService: ArbeidsgiverService
){
    route(OPPSLAG_URL){
        søkerApis(søkerService, idTokenProvider)
        barnApis(barnservice, idTokenProvider)
        arbeidsgiverApis(arbeidsgiverService, idTokenProvider)
    }
}
