package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_FORDELING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_KORONAOVERFORING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_OVERFORING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.OmsorgsdagerMelding
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgsdagermelding.omsorgsdagerMeldingApi")

fun Route.omsorgsdagerMeldingApi(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
) {
    post(OMSORGSDAGER_MELDING_FORDELING_URL + INNSENDING_URL) {
        mottaMelding(innsendingService, barnService, innsendingCache, idTokenProvider)
    }

    post(OMSORGSDAGER_MELDING_OVERFORING_URL + INNSENDING_URL) {
        mottaMelding(innsendingService, barnService, innsendingCache, idTokenProvider)
    }

    post(OMSORGSDAGER_MELDING_KORONAOVERFORING_URL + INNSENDING_URL) {
        mottaMelding(innsendingService, barnService, innsendingCache, idTokenProvider)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.mottaMelding(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider
) {
    val melding = call.receive<OmsorgsdagerMelding>()
    val callId = call.getCallId()
    val metadata = call.getMetadata()
    val idToken = idTokenProvider.getIdToken(call)
    val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${melding.ytelse()}"

    logger.info(formaterStatuslogging(melding.ytelse(), melding.søknadId, "mottatt."))

    melding.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

    innsendingCache.put(cacheKey)
    innsendingService.registrer(melding, callId, idToken, metadata)
    registrerMottattSøknad(melding.ytelse())
    call.respond(HttpStatusCode.Accepted)
}
