package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_SNF_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutbetalingsnf.omsorgspengerUtbetalingSnfApis")

fun Route.omsorgspengerUtbetalingSnfApis(
    idTokenProvider: IdTokenProvider,
    omsorgspengerUtbetalingSnfService: OmsorgspengerUtbetalingSnfService
) {
    route(OMSORGSPENGER_UTBETALING_SNF_URL){
        post(INNSENDING_URL){
            val søknad = call.receive<Søknad>()
            logger.info(formaterStatuslogging(Ytelse.OMSORGSDAGER_ALENEOMSORG, søknad.søknadId.id, "mottatt."))
            omsorgspengerUtbetalingSnfService.registrer(søknad, call.getCallId(), call.getMetadata(), idTokenProvider.getIdToken(call))
            registrerMottattSøknad(Ytelse.OMSORGSPENGER_UTBETALING_SNF)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}