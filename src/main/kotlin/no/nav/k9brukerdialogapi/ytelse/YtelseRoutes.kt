package no.nav.k9brukerdialogapi.ytelse

import io.ktor.server.routing.Route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.innsyn.InnsynService
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.ettersending.ettersendingApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.omsorgspengerUtbetalingArbeidstakerApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.omsorgspengerUtbetalingSnfApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.omsorgspengerUtvidetRettApis
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.pleiepengerLivetsSluttfaseApi
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.endringsmelding.endringsmeldingApis
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.pleiepengerSyktBarnApi

fun Route.ytelseRoutes(
    idTokenProvider: IdTokenProvider,
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    innsynService: InnsynService
){
    pleiepengerLivetsSluttfaseApi(innsendingService, innsendingCache, idTokenProvider)
    omsorgspengerUtbetalingArbeidstakerApi(innsendingService, innsendingCache, idTokenProvider)
    ettersendingApis(innsendingService, barnService, innsendingCache, idTokenProvider)
    omsorgspengerUtvidetRettApis(innsendingService, barnService, innsendingCache, idTokenProvider)
    omsorgspengerUtbetalingSnfApis(innsendingService, barnService, innsendingCache, idTokenProvider)
    omsorgspengerMidlertidigAleneApis(innsendingService, barnService, innsendingCache, idTokenProvider)
    omsorgsdagerAleneomsorgApis(innsendingService, barnService, innsendingCache, idTokenProvider)
    pleiepengerSyktBarnApi(innsendingService, barnService, innsendingCache, idTokenProvider)
    endringsmeldingApis(innsendingService, innsendingCache, innsynService, idTokenProvider)
}
