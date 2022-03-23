package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.oppslag.søker.valider
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.valider
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTVIDET_RETT
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Barn.Companion.leggTilIdentifikatorPåBarnSomMangler
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

        håndterBarn(søknad, idToken, callId)

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        val k9Format = søknad.tilK9Format(søker)
        validerK9Format(k9Format)
        søknad.valider()

        val dokumentEier = DokumentEier(søker.fødselsnummer)
        validerVedlegg(søknad, idToken, callId, dokumentEier)
        persisterVedlegg(søknad, callId, dokumentEier)

        val komplettSøknad = søknad.tilKomplettSøknad(søker, k9Format)
        try {
            kafkaProdusent.produserKafkaMelding(metadata, JSONObject(komplettSøknad.somJson()), OMSORGSPENGER_UTVIDET_RETT)
        } catch (e: Exception) {
            logger.info("Feilet ved å legge melding på Kafka.")
            if(søknad.legeerklæring.isNotEmpty()){
                logger.info("Fjerner hold på persisterte vedlegg.")
                vedleggService.fjernHoldPåPersistertVedlegg(søknad.legeerklæring, callId, dokumentEier)

                if(søknad.samværsavtale != null && søknad.samværsavtale.isNotEmpty()){
                    vedleggService.fjernHoldPåPersistertVedlegg(søknad.samværsavtale, callId, dokumentEier)
                }
            }
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    private suspend fun håndterBarn(søknad: Søknad, idToken: IdToken, callId: CallId) {
        if (søknad.barn.manglerIdentifikator()) {
            logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "oppdaterer id på barn."))
            leggTilIdentifikatorPåBarnSomMangler(søknad.barn, barnService.hentBarn(idToken, callId))
        }
    }

    private suspend fun validerVedlegg(søknad: Søknad, idToken: IdToken, callId: CallId, dokumentEier: DokumentEier) {
        if (søknad.legeerklæring.isNotEmpty()) {
            logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "validerer legeerklæring."))
            vedleggService.hentVedlegg(søknad.legeerklæring, idToken, callId, dokumentEier)
                .valider("legeerklæring", søknad.legeerklæring)
        }

        if(søknad.samværsavtale != null && søknad.samværsavtale.isNotEmpty()){
            logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "validerer samværsavtale."))
            vedleggService.hentVedlegg(søknad.samværsavtale, idToken, callId, dokumentEier)
                .valider("samværsavtale", søknad.legeerklæring)
        }
    }

    private suspend fun persisterVedlegg(søknad: Søknad, callId: CallId, dokumentEier: DokumentEier){
        if(søknad.legeerklæring.isNotEmpty()){
            logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "persisterer legeerklæring."))
            vedleggService.persisterVedlegg(søknad.legeerklæring, callId, dokumentEier)
        }

        if(søknad.samværsavtale != null && søknad.samværsavtale.isNotEmpty()){
            logger.info(formaterStatuslogging(OMSORGSPENGER_UTVIDET_RETT, søknad.søknadId, "persisterer samværsavtale."))
            vedleggService.persisterVedlegg(søknad.samværsavtale, callId, dokumentEier)
        }
    }
}

class MeldingRegistreringFeiletException(s: String) : Throwable(s)