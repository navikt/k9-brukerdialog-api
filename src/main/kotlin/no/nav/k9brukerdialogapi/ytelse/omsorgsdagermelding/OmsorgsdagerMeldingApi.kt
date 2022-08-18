package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_FORDELING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_KORONAOVERFORING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_OVERFORING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse.*
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Melding
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgsdagermelding.omsorgsdagerMeldingApi")

fun Route.omsorgsdagerMeldingApi(
    idTokenProvider: IdTokenProvider,
    omsorgsdagerMeldingService: OmsorgsdagerMeldingService
) {
    post(OMSORGSDAGER_MELDING_FORDELING_URL+INNSENDING_URL){
        mottaMelding(omsorgsdagerMeldingService, idTokenProvider)
        registrerMottattSøknad(OMSORGSDAGER_MELDING_FORDELING)
    }

    post(OMSORGSDAGER_MELDING_OVERFORING_URL+INNSENDING_URL){
        mottaMelding(omsorgsdagerMeldingService, idTokenProvider)
        registrerMottattSøknad(OMSORGSDAGER_MELDING_OVERFORING)
    }

    post(OMSORGSDAGER_MELDING_KORONAOVERFORING_URL+INNSENDING_URL){
        mottaMelding(omsorgsdagerMeldingService, idTokenProvider)
        registrerMottattSøknad(OMSORGSDAGER_MELDING_KORONAOVERFORING)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.mottaMelding(
    omsorgsdagerMeldingService: OmsorgsdagerMeldingService,
    idTokenProvider: IdTokenProvider
) {
    val melding = call.receive<Melding>()
    logger.info(formaterStatuslogging(melding.type.somYtelse(), melding.søknadId, "mottatt."))
    omsorgsdagerMeldingService.registrer(melding, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
    call.respond(HttpStatusCode.Accepted)
}