package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.PLEIEPENGER_LIVETS_SLUTTFASE_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PilsSøknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.pleiepengerLivetsSluttfaseApi(
    innsendingService: InnsendingService,
    idTokenProvider: IdTokenProvider,
){
    val logger: Logger = LoggerFactory.getLogger("ytelse.pleiepengerlivetssluttfase.pleiepengerLivetsSluttfaseApi.kt")

    route(PLEIEPENGER_LIVETS_SLUTTFASE_URL){
        post(INNSENDING_URL){
            val pilsSøknad = call.receive<PilsSøknad>()
            logger.info(formaterStatuslogging(pilsSøknad.ytelse(), pilsSøknad.søknadId, "mottatt."))
            innsendingService.registrer(pilsSøknad, call.getCallId(), idTokenProvider.getIdToken(call), call.getMetadata())
            registrerMottattSøknad(pilsSøknad.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
