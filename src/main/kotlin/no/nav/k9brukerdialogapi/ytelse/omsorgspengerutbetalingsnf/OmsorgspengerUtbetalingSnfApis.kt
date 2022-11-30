package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_SNF_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.OmsorgspengerutbetalingSnfSøknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutbetalingsnf.omsorgspengerUtbetalingSnfApis")

fun Route.omsorgspengerUtbetalingSnfApis(
    innsendingService: InnsendingService,
    barnService: BarnService,
    idTokenProvider: IdTokenProvider,
) {
    route(OMSORGSPENGER_UTBETALING_SNF_URL){
        post(INNSENDING_URL){
            val søknad = call.receive<OmsorgspengerutbetalingSnfSøknad>()
            val callId = call.getCallId()
            val metadata = call.getMetadata()
            val idToken = idTokenProvider.getIdToken(call)

            logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId.id, "mottatt."))
            søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

            innsendingService.registrer(søknad, callId, idToken, metadata)
            registrerMottattSøknad(søknad.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
