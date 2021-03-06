package no.nav.k9brukerdialogapi.ytelse.ettersending

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.ETTERSENDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse.ETTERSENDING
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.ettersending.ettersendingApis.kt")

fun Route.ettersendingApis(
    idTokenProvider: IdTokenProvider,
    ettersendingService: EttersendingService
){
    route(ETTERSENDING_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<Søknad>()
            logger.info(formaterStatuslogging(ETTERSENDING, søknad.søknadId, "mottatt."))
            logger.info("Ettersending for ytelse ${søknad.søknadstype}")
            ettersendingService.registrer(søknad, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            registrerMottattSøknad(ETTERSENDING)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}