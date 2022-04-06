package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.Søknad
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OmsorgsdagerAleneomsorgService(
    private val kafkaProdusent: KafkaProducer,
    private val søkerService: SøkerService,
    private val barnService: BarnService
) {
    private val logger: Logger = LoggerFactory.getLogger(OmsorgsdagerAleneomsorgService::class.java)
    private val YTELSE = Ytelse.OMSORGSDAGER_ALENEOMSORG

    suspend fun registrer(søknad: Søknad, callId: CallId, metadata: Metadata, idToken: IdToken) {
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId).also { it.valider() }

        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

        søknad.valider()
        if (søknad.gjelderFlereBarn()) registrerFlereSøknader(metadata, søknad, søker)
        else registrerEnSøknad(metadata, søknad, søker)
    }

    private fun registrerFlereSøknader(metadata: Metadata, søknad: Søknad, søker: Søker) {
        val søknader = søknad.splittTilEgenSøknadPerBarn()
        logger.info("SøknadId:${søknad.søknadId} splittet ut til ${søknader.map { it.søknadId }}")

        val komplettSøknaderSomJSONObject = søknader.map {
            val k9Format = it.somK9Format(søker)
            val komplettSøknad = it.somKomplettSøknad(søker, k9Format)
            JSONObject(komplettSøknad.somJson())
        }

        kafkaProdusent.produserKafkaMeldinger(metadata, komplettSøknaderSomJSONObject, YTELSE)
    }

    private fun registrerEnSøknad(metadata: Metadata, søknad: Søknad, søker: Søker) {
        val k9Format = søknad.somK9Format(søker).also { validerK9Format(it) }
        kafkaProdusent.produserKafkaMelding(
            metadata,
            JSONObject(søknad.somKomplettSøknad(søker, k9Format).somJson()),
            YTELSE
        )
    }
}