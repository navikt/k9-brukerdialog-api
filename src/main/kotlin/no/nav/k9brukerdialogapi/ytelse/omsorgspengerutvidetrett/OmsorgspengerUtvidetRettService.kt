package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTVIDET_RETT
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OmsorgspengerUtvidetRettService(
    private val søkerService: SøkerService,
    private val barnService: BarnService,
    private val vedleggService: VedleggService,
    private val kafkaProdusent: KafkaProducer
) {
    private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtvidetRettService::class.java)

    suspend fun registrer(søknad: Søknad, callId: CallId, metadata: Metadata, idToken: IdToken) {
        logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

        val k9Format = søknad.tilK9Format(søker)
        validerK9Format(k9Format)
        søknad.valider()

        val dokumentEier = søker.somDokumentEier()
        søknad.validerVedlegg(vedleggService, idToken, callId, dokumentEier)
        søknad.persisterVedlegg(vedleggService, callId, dokumentEier)

        try {
            kafkaProdusent.produserKafkaMelding(
                metadata,
                JSONObject(søknad.tilKomplettSøknad(søker, k9Format).somJson()),
                OMSORGSPENGER_UTVIDET_RETT
            )
        } catch (e: Exception) {
            logger.info("Feilet ved å legge melding på Kafka.")
            søknad.fjernHoldPåPersisterteVedlegg(vedleggService, callId, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }
}

class MeldingRegistreringFeiletException(s: String) : Throwable(s)