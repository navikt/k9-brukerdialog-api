package no.nav.k9brukerdialogapi.ytelse.opplaeringspenger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL
import no.nav.k9brukerdialogapi.OPPLAERINGSPENGER_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.opplaeringspenger.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.opplaeringspenger.opplaeringspengerApi.kt")

fun Route.opplaeringspengerApi(
    idTokenProvider: IdTokenProvider,
    opplaeringspengerService: OpplaeringspengerService
) {
    route(OPPLAERINGSPENGER_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<Søknad>()
            logger.info(formaterStatuslogging(Ytelse.OPPLAERINGSPENGER, søknad.søknadId, "mottatt."))
            opplaeringspengerService.registrer(søknad, idTokenProvider.getIdToken(call), call.getCallId(), call.getMetadata())
            registrerMottattSøknad(Ytelse.OPPLAERINGSPENGER)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
