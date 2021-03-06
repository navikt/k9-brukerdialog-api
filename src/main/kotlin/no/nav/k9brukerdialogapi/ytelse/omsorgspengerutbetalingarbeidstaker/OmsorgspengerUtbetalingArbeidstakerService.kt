package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.MeldingRegistreringFeiletException
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.valider
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Søknad
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OmsorgspengerUtbetalingArbeidstakerService(
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService,
    private val kafkaProdusent: KafkaProducer
) {
    private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtbetalingArbeidstakerService::class.java)
    private val YTELSE = OMSORGSPENGER_UTBETALING_ARBEIDSTAKER

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        metadata: Metadata
    ){
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))
        søknad.valider()

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        val k9Format = søknad.tilK9Format(søker)
        validerK9FormatForOMP_UT(k9Format)

        if(søknad.vedlegg.isNotEmpty()){
            registrerSøknadMedVedlegg(søknad, idToken, callId, søker, k9Format, metadata)
        } else {
            registrerSøknadUtenVedlegg(søknad, søker, k9Format, metadata)
        }
    }

    private fun registrerSøknadUtenVedlegg(
        søknad: Søknad,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Søknad,
        metadata: Metadata
    ) {
        val komplettSøknad = søknad.tilKomplettSøknad(søker, k9Format)
        try {
            kafkaProdusent.produserKafkaMelding(
                metadata,
                JSONObject(komplettSøknad.somJson()),
                OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
            )
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.")
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    private suspend fun registrerSøknadMedVedlegg(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Søknad,
        metadata: Metadata
    ) {
        logger.info("Validerer ${søknad.vedlegg.size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(søknad.vedlegg, idToken, callId)
        vedlegg.valider("vedlegg", søknad.vedlegg)
        vedleggService.persisterVedlegg(søknad.vedlegg, callId, søker.somDokumentEier())
        val komplettSøknad = søknad.tilKomplettSøknad(søker, k9Format, vedlegg.map { it.title })

        try {
            kafkaProdusent.produserKafkaMelding(metadata, JSONObject(komplettSøknad.somJson()), OMSORGSPENGER_UTBETALING_ARBEIDSTAKER)
        } catch (exception: Exception){
            logger.error("Feilet ved å legge melding på Kafka.")
            logger.info("Fjerner hold på persisterte vedlegg")
            vedleggService.fjernHoldPåPersistertVedlegg(søknad.vedlegg, callId, søker.somDokumentEier())
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }
}