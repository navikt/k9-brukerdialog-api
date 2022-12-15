package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.PLEIEPENGER_SYKT_BARN_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.pleiepengerSyktBarnApi(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
){
    val logger: Logger = LoggerFactory.getLogger("ytelse.pleiepengersyktbarn.PleiepengerSyktBarnApiKt")

    route(PLEIEPENGER_SYKT_BARN_URL){
        post(INNSENDING_URL){
            val søknad = call.receive<Søknad>()
            val idToken = idTokenProvider.getIdToken(call)
            val callId = call.getCallId()
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${søknad.ytelse()}"

            logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))
            søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

            innsendingService.registrer(søknad, callId, idToken, call.getMetadata())
            registrerMottattSøknad(søknad.ytelse())
            innsendingCache.put(cacheKey)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
