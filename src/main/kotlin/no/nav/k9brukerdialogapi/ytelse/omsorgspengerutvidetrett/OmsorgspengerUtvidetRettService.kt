package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.MeldingRegistreringFeiletException
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.valider
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
    private val YTELSE = OMSORGSPENGER_UTVIDET_RETT

    suspend fun registrer(søknad: Søknad, callId: CallId, metadata: Metadata, idToken: IdToken) {
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))

        val k9Format = søknad.tilK9Format(søker)
        validerK9Format(k9Format)
        søknad.valider()

        val dokumentEier = søker.somDokumentEier()
        validerVedlegg(søknad, idToken, callId)
        persisterVedlegg(søknad, callId, dokumentEier)

        try {
            kafkaProdusent.produserKafkaMelding(
                metadata,
                JSONObject(søknad.tilKomplettSøknad(søker, k9Format).somJson()),
                YTELSE
            )
        } catch (e: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.")
            fjernHoldPåPersisterteVedlegg(søknad, callId, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    suspend fun persisterVedlegg(søknad: Søknad, callId: CallId, dokumentEier: DokumentEier){
        logger.info("Persisterer vedlegg")
        if(søknad.legeerklæring.isNotEmpty()) {
            vedleggService.persisterVedlegg(søknad.legeerklæring, callId, dokumentEier)
        }
        if (søknad.samværsavtale != null && søknad.samværsavtale.isNotEmpty()) {
            vedleggService.persisterVedlegg(søknad.samværsavtale, callId, dokumentEier)
        }
    }

    suspend fun validerVedlegg(søknad: Søknad, idToken: IdToken, callId: CallId) {
        logger.info("Validerer vedlegg")
        if(søknad.legeerklæring.isNotEmpty()) {
            vedleggService.hentVedlegg(søknad.legeerklæring, idToken, callId)
                .valider("legeerklæring", søknad.legeerklæring)
        }
        if (søknad.samværsavtale != null && søknad.samværsavtale.isNotEmpty()) {
            vedleggService.hentVedlegg(søknad.samværsavtale, idToken, callId)
                .valider("samværsavtale", søknad.samværsavtale)
        }
    }

    suspend fun fjernHoldPåPersisterteVedlegg(søknad: Søknad,  callId: CallId, dokumentEier: DokumentEier){
        logger.info("Fjerner hold på persisterte vedlegg.")
        if (søknad.legeerklæring.isNotEmpty()) vedleggService.fjernHoldPåPersistertVedlegg(søknad.legeerklæring, callId, dokumentEier)
        if (søknad.samværsavtale != null && søknad.samværsavtale.isNotEmpty()) {
            vedleggService.fjernHoldPåPersistertVedlegg(søknad.samværsavtale, callId, dokumentEier)
        }
    }
}