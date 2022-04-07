package no.nav.k9brukerdialogapi.ytelse

import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.ytelse.ettersending.EttersendingService
import no.nav.k9brukerdialogapi.ytelse.ettersending.ettersendingApis
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.OmsorgsdagerAleneomsorgService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.OmsorgspengerMidlertidigAleneService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.omsorgspengerUtvidetRettApis

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
}