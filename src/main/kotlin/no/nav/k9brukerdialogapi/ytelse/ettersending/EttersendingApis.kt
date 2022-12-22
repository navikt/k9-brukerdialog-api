package no.nav.k9brukerdialogapi.ytelse.ettersending

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.ETTERSENDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.ettersending.ettersendingApis.kt")

fun Route.ettersendingApis(
    innsendingService: InnsendingService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
){
    route(ETTERSENDING_URL){
        post(INNSENDING_URL){
            val ettersendelse =  call.receive<Ettersendelse>()
            val idToken = idTokenProvider.getIdToken(call)
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${ettersendelse.ytelse()}"


            logger.info(formaterStatuslogging(ettersendelse.ytelse(), ettersendelse.søknadId, "mottatt."))
            logger.info("Ettersending for ytelse ${ettersendelse.søknadstype}")
            innsendingCache.put(cacheKey)
            innsendingService.registrer(ettersendelse, call.getCallId(), idToken, call.getMetadata())
            registrerMottattSøknad(ettersendelse.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
