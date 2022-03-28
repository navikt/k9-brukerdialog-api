package no.nav.k9brukerdialogapi.ytelse

import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.OmsorgspengerMidlertidigAleneService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.omsorgspengerUtvidetRettApis

fun Route.ytelseRoutes(
    idTokenProvider: IdTokenProvider,
    omsorgspengerUtvidetRettService: OmsorgspengerUtvidetRettService,
    omsorgspengerMidlertidigAleneService: OmsorgspengerMidlertidigAleneService
){
    omsorgspengerUtvidetRettApis(omsorgspengerUtvidetRettService, idTokenProvider)
    omsorgspengerMidlertidigAleneApis(omsorgspengerMidlertidigAleneService, idTokenProvider)
}