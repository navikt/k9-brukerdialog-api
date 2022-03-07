package no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett

import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider

fun Route.ytelseRoutes(
    idTokenProvider: IdTokenProvider,
    omsorgspengerUtvidetRettService: OmsorgspengerUtvidetRettService
){
    omsorgspengerUtvidetRettApis(omsorgspengerUtvidetRettService, idTokenProvider)
}