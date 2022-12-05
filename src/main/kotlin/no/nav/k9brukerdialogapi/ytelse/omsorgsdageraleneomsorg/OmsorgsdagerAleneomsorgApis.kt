package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSDAGER_ALENEOMSORG_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.OmsorgsdagerAleneOmOmsorgenSøknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgsdageraleneomsorg.omsorgsdagerAleneomsorgApis.kt")

fun Route.omsorgsdagerAleneomsorgApis(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
){
    route(OMSORGSDAGER_ALENEOMSORG_URL){
        post(INNSENDING_URL){
            val søknad = call.receive<OmsorgsdagerAleneOmOmsorgenSøknad>()
            val callId = call.getCallId()
            val metadata = call.getMetadata()
            val idToken = idTokenProvider.getIdToken(call)

            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${søknad.ytelse()}"
            innsendingCache.put(cacheKey)

            logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))

            søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

            if (søknad.gjelderFlereBarn()) {
                val søknader = søknad.splittTilEgenSøknadPerBarn()
                logger.info("SøknadId:${søknad.søknadId} splittet ut til ${søknader.map { it.søknadId }}")
                søknader.forEach {
                    innsendingService.registrer(it, callId, idToken, metadata)
                }
            } else {
                innsendingService.registrer(søknad, callId, idToken, metadata)
            }
            registrerMottattSøknad(søknad.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
