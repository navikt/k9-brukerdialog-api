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
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnSøknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettApis.kt")

fun Route.omsorgspengerUtvidetRettApis(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider
){
    route(OMSORGSPENGER_UTVIDET_RETT_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<OmsorgspengerKroniskSyktBarnSøknad>()
            val callId = call.getCallId()
            val idToken = idTokenProvider.getIdToken(call)
            val metadata = call.getMetadata()
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${søknad.ytelse()}"

            logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))
            søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

            innsendingService.registrer(søknad, callId, idToken, metadata)
            registrerMottattSøknad(søknad.ytelse())
            innsendingCache.put(cacheKey)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
