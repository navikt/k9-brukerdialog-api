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
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.ettersending.ettersendingApis.kt")

fun Route.ettersendingApis(
    innsendingService: InnsendingService,
    idTokenProvider: IdTokenProvider,
){
    route(ETTERSENDING_URL){
        post(INNSENDING_URL){
            val ettersendelse =  call.receive<Ettersendelse>()
            logger.info(formaterStatuslogging(ettersendelse.ytelse(), ettersendelse.søknadId, "mottatt."))
            logger.info("Ettersending for ytelse ${ettersendelse.søknadstype}")
            innsendingService.registrer(ettersendelse, call.getCallId(),  idTokenProvider.getIdToken(call), call.getMetadata())
            registrerMottattSøknad(ettersendelse.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
