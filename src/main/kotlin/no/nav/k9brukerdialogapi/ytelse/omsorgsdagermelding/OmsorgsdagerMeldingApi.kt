package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_MELDING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Melding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgsdagermelding.omsorgsdagerMeldingApi")

fun Route.omsorgsdagerMeldingApi(
    idTokenProvider: IdTokenProvider,
    omsorgsdagerMeldingService: OmsorgsdagerMeldingService
) {
    route(OMSORGSDAGER_MELDING_URL){
        post(INNSENDING_URL){
            val melding = call.receive<Melding>()
            logger.info(formaterStatuslogging(melding.type.somYtelse(), melding.søknadId, "mottatt."))
            omsorgsdagerMeldingService.registrer(melding, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            call.respond(HttpStatusCode.Accepted)
        }
    }
}