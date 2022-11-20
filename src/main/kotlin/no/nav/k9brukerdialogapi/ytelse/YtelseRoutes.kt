package no.nav.k9brukerdialogapi.ytelse

import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.ytelse.ettersending.EttersendingService
import no.nav.k9brukerdialogapi.ytelse.ettersending.ettersendingApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.OmsorgsdagerAleneomsorgService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.OmsorgsdagerMeldingService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.omsorgsdagerMeldingApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.OmsorgspengerMidlertidigAleneService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.OmsorgspengerUtbetalingArbeidstakerService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.omsorgspengerUtbetalingArbeidstakerApi
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.OmsorgspengerUtbetalingSnfService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.omsorgspengerUtbetalingSnfApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.omsorgspengerUtvidetRettApis
import no.nav.k9brukerdialogapi.ytelse.opplaeringspenger.OpplaeringspengerService
import no.nav.k9brukerdialogapi.ytelse.opplaeringspenger.opplaeringspengerApi
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.PleiepengerLivetsSluttfaseService
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.pleiepengerLivetsSluttfaseApi

fun Route.ytelseRoutes(
    idTokenProvider: IdTokenProvider,
    kafkaProdusent: KafkaProducer,
    barnService: BarnService,
    søkerService: SøkerService,
    vedleggService: VedleggService
){
    omsorgspengerUtvidetRettApis(
        OmsorgspengerUtvidetRettService(søkerService, barnService, vedleggService, kafkaProdusent),
        idTokenProvider
    )
    omsorgspengerMidlertidigAleneApis(
        OmsorgspengerMidlertidigAleneService(søkerService, barnService, kafkaProdusent),
        idTokenProvider
    )
    ettersendingApis(
        idTokenProvider,
        EttersendingService(kafkaProdusent, søkerService, vedleggService)
    )
    omsorgsdagerAleneomsorgApis(
        idTokenProvider,
        OmsorgsdagerAleneomsorgService(kafkaProdusent, søkerService, barnService)
    )
    omsorgspengerUtbetalingArbeidstakerApi(
        idTokenProvider,
        OmsorgspengerUtbetalingArbeidstakerService(søkerService, vedleggService, kafkaProdusent)
    )
    omsorgspengerUtbetalingSnfApis(
        idTokenProvider,
        OmsorgspengerUtbetalingSnfService(søkerService, barnService, vedleggService, kafkaProdusent)
    )
    omsorgsdagerMeldingApi(
        idTokenProvider,
        OmsorgsdagerMeldingService(søkerService, barnService, kafkaProdusent, vedleggService)
    )
    pleiepengerLivetsSluttfaseApi(
        idTokenProvider,
        PleiepengerLivetsSluttfaseService(søkerService, kafkaProdusent, vedleggService)
    )
    opplaeringspengerApi(
        idTokenProvider,
        OpplaeringspengerService(søkerService, vedleggService, kafkaProdusent)
    )
}