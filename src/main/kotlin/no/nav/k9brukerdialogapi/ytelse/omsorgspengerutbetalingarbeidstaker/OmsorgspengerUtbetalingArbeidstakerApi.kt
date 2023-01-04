package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.OmsorgspengerutbetalingArbeidstakerSøknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutbetalingarbeidstaker.omsorgspengerUtbetalingArbeidstakerApi.kt")

fun Route.omsorgspengerUtbetalingArbeidstakerApi(
    innsendingService: InnsendingService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
) {
    route(OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<OmsorgspengerutbetalingArbeidstakerSøknad>()
            val idToken = idTokenProvider.getIdToken(call)
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${søknad.ytelse()}"

            logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))

            innsendingCache.put(cacheKey)
            innsendingService.registrer(søknad, call.getCallId(), idToken, call.getMetadata())
            registrerMottattSøknad(søknad.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
