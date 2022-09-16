package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.MeldingRegistreringFeiletException
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.valider
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Søknad
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PleiepengerLivetsSluttfaseService(
    private val søkerService: SøkerService,
    private val kafkaProdusent: KafkaProducer,
    private val vedleggService: VedleggService
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val YTELSE = Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE

    internal suspend fun registrer(søknad: Søknad, callId: CallId, idToken: IdToken, metadata: Metadata){
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        val k9Format = søknad.somK9Format(søker)
        validerK9FormatPILS(k9Format)
        søknad.valider()

        val dokumentEier = søker.somDokumentEier()
        if(søknad.inneholderVedlegg()){
            validerVedlegg(søknad, idToken, callId)
            persisterVedlegg(søknad, callId, dokumentEier)
        }

        try {
            kafkaProdusent.produserKafkaMelding(metadata = metadata, ytelse = YTELSE,
                komplettSøknadSomJson = JSONObject(søknad.somKomplettSøknad(søker, k9Format).somJson())
            )
        } catch (e: Exception) {
            logger.info("Feilet med å legge søknad på Kafka.")
            if(søknad.inneholderVedlegg()) fjernHoldPåPersisterteVedlegg(søknad, callId, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet med å legge søknad på Kafka.")
        }
    }

    private suspend fun validerVedlegg(søknad: Søknad, idToken: IdToken, callId: CallId) {
        if(søknad.vedleggUrls.isNotEmpty()){
            logger.info("Validerer vedleggUrls")
            vedleggService.hentVedlegg(søknad.vedleggUrls, idToken, callId)
                .valider("vedleggUrls", søknad.vedleggUrls)
        }

        if(søknad.opplastetIdVedleggUrls.isNotEmpty()){
            logger.info("Validerer opplastetIdVedleggUrls")
            vedleggService.hentVedlegg(søknad.opplastetIdVedleggUrls, idToken, callId)
                .valider("opplastetIdVedleggUrls", søknad.opplastetIdVedleggUrls)
        }
    }

    private suspend fun persisterVedlegg(søknad: Søknad, callId: CallId, eier: DokumentEier) {
        if(søknad.vedleggUrls.isNotEmpty()){
            logger.info("Persisterer vedleggUrls")
            vedleggService.persisterVedlegg(søknad.vedleggUrls, callId, eier)
        }

        if(søknad.opplastetIdVedleggUrls.isNotEmpty()){
            logger.info("Persisterer opplastetIdVedleggUrls")
            vedleggService.persisterVedlegg(søknad.opplastetIdVedleggUrls, callId, eier)
        }
    }

    private suspend fun fjernHoldPåPersisterteVedlegg(søknad: Søknad, callId: CallId, eier: DokumentEier) {
        if(søknad.vedleggUrls.isNotEmpty()){
            logger.info("Fjerner hold på persisterte vedleggUrls")
            vedleggService.fjernHoldPåPersistertVedlegg(søknad.vedleggUrls, callId, eier)
        }

        if(søknad.opplastetIdVedleggUrls.isNotEmpty()){
            logger.info("Fjerner hold på persisterte opplastetIdVedleggUrls")
            vedleggService.fjernHoldPåPersistertVedlegg(søknad.opplastetIdVedleggUrls, callId, eier)
        }
    }
}