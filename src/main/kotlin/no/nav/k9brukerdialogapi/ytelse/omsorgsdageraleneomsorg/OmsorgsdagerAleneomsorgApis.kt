package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_ALENEOMSORG_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSDAGER_ALENEOMSORG
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis.kt")

fun Route.omsorgsdagerAleneomsorgApis(
    idTokenProvider: IdTokenProvider,
    omsorgsdagerAleneomsorgService: OmsorgsdagerAleneomsorgService
){
    route(OMSORGSDAGER_ALENEOMSORG_URL){
        post(INNSENDING_URL){
            val søknad = call.receive<Søknad>()
            logger.info(formaterStatuslogging(OMSORGSDAGER_ALENEOMSORG, søknad.søknadId, "mottatt."))
            omsorgsdagerAleneomsorgService.registrer(søknad, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            registrerMottattSøknad(OMSORGSDAGER_ALENEOMSORG)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}