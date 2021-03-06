package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTVIDET_RETT_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTVIDET_RETT
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettApis.kt")

fun Route.omsorgspengerUtvidetRettApis(
    omsorgspengerUtvidetRettService: OmsorgspengerUtvidetRettService,
    idTokenProvider: IdTokenProvider
){
    route(OMSORGSPENGER_UTVIDET_RETT_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<Søknad>()
            logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "mottatt."))
            omsorgspengerUtvidetRettService.registrer(søknad, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            registrerMottattSøknad(OMSORGSPENGER_UTVIDET_RETT)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}