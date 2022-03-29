package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Søknad
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OmsorgspengerMidlertidigAleneService(
    private val søkerService: SøkerService,
    private val barnService: BarnService,
    private val kafkaProdusent: KafkaProducer
) {
    private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerMidlertidigAleneService::class.java)
    private val YTELSE = Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE

    suspend fun registrer(søknad: Søknad, callId: CallId, metadata: Metadata, idToken: IdToken) {
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

        val k9Format = søknad.tilK9Format(søker)
        validerK9Format(k9Format)
        søknad.valider()

        kafkaProdusent.produserKafkaMelding(
            metadata,
            JSONObject(søknad.tilKomplettSøknad(søker, k9Format).somJson()),
            YTELSE
        )
    }
}