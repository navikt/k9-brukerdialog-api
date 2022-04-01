package no.nav.k9brukerdialogapi.ytelse.ettersending

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.ETTERSENDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknad
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
            logger.info(formaterStatuslogging(Ytelse.ETTERSENDING, søknad.søknadId, "mottatt."))
            ettersendingService.registrer(søknad, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            call.respond(HttpStatusCode.Accepted)
        }
    }
}