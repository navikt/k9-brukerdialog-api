package no.nav.k9brukerdialogapi.ytelse

import io.ktor.server.routing.Route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.ettersending.ettersendingApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.omsorgsdagerMeldingApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.omsorgspengerUtbetalingArbeidstakerApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.omsorgspengerUtbetalingSnfApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.omsorgspengerUtvidetRettApis
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.pleiepengerLivetsSluttfaseApi

fun Route.ytelseRoutes(
    idTokenProvider: IdTokenProvider,
    innsendingService: InnsendingService,
    barnService: BarnService
){
    pleiepengerLivetsSluttfaseApi(innsendingService, idTokenProvider)
    omsorgspengerUtbetalingArbeidstakerApi(innsendingService, idTokenProvider)
    ettersendingApis(innsendingService, idTokenProvider)
    omsorgspengerUtvidetRettApis(innsendingService, barnService, idTokenProvider)
    omsorgspengerUtbetalingSnfApis(innsendingService, barnService, idTokenProvider)
    omsorgspengerMidlertidigAleneApis(innsendingService, barnService, idTokenProvider)
    omsorgsdagerMeldingApi(innsendingService, barnService, idTokenProvider)
    omsorgsdagerAleneomsorgApis(innsendingService, barnService, idTokenProvider)
}
