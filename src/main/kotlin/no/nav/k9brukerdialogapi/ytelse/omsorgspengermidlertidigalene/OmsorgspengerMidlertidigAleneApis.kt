package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_MIDLERTIDIG_ALENE_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengermidlertidigalene.omsorgspengerMidlertidigAleneApis.kt")

fun Route.omsorgspengerMidlertidigAleneApis(
    omsorgspengerMidlertidigAleneService: OmsorgspengerMidlertidigAleneService,
    idTokenProvider: IdTokenProvider
){
    route(OMSORGSPENGER_MIDLERTIDIG_ALENE_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<Søknad>()
            logger.info(formaterStatuslogging(OMSORGSPENGER_MIDLERTIDIG_ALENE, søknad.søknadId, "mottatt."))
            omsorgspengerMidlertidigAleneService.registrer(søknad, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            registrerMottattSøknad(OMSORGSPENGER_MIDLERTIDIG_ALENE)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}