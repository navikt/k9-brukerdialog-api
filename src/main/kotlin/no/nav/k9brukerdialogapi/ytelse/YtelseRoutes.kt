package no.nav.k9brukerdialogapi.ytelse

import io.ktor.server.routing.Route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.ytelse.ettersending.EttersendingService
import no.nav.k9brukerdialogapi.ytelse.ettersending.ettersendingApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.OmsorgsdagerAleneomsorgService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.omsorgsdagerMeldingApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.omsorgspengerUtbetalingArbeidstakerApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.omsorgspengerUtbetalingSnfApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.omsorgspengerUtvidetRettApis
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.pleiepengerLivetsSluttfaseApi

fun Route.ytelseRoutes(
    idTokenProvider: IdTokenProvider,
    kafkaProdusent: KafkaProducer,
    barnService: BarnService,
    søkerService: SøkerService,
    vedleggService: VedleggService
){
    val innsendingService = InnsendingService(søkerService, kafkaProdusent, vedleggService)

    pleiepengerLivetsSluttfaseApi(idTokenProvider, innsendingService)
    omsorgspengerUtvidetRettApis(innsendingService, barnService, idTokenProvider)
    omsorgspengerUtbetalingSnfApis(idTokenProvider, barnService, innsendingService)
    omsorgspengerUtbetalingArbeidstakerApi(idTokenProvider, innsendingService)
    omsorgspengerMidlertidigAleneApis(innsendingService, barnService, idTokenProvider)
    omsorgsdagerMeldingApi(idTokenProvider, innsendingService, barnService)

    ettersendingApis(
        idTokenProvider,
        EttersendingService(kafkaProdusent, søkerService, vedleggService)
    )
    omsorgsdagerAleneomsorgApis(
        idTokenProvider,
        OmsorgsdagerAleneomsorgService(kafkaProdusent, søkerService, barnService)
    )
}
