package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.valider
import no.nav.k9brukerdialogapi.ytelse.Ytelse
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
    private val YTELSE = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        metadata: Metadata
    ){
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        val k9Format = søknad.tilK9Format(søker)
        validerK9FormatForOMP_UT(k9Format)

        if(søknad.vedlegg.isNotEmpty()){
            registrerSøknadMedVedlegg(søknad, idToken, callId, søker, k9Format, metadata)
        } else {
            registrerSøknadUtenVedlegg()
        }
    }

    private fun registrerSøknadUtenVedlegg() {

    }

    private suspend fun registrerSøknadMedVedlegg(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Søknad,
        metadata: Metadata
    ) {
        val vedlegg = vedleggService.hentVedlegg(søknad.vedlegg, idToken, callId, søker.somDokumentEier())
        vedlegg.valider("vedlegg", søknad.vedlegg)
        vedleggService.persisterVedlegg(søknad.vedlegg, callId, søker.somDokumentEier())
        val komplettSøknad = søknad.tilKomplettSøknad(søker, k9Format, vedlegg.map { it.title })

        try {
            kafkaProdusent.produserKafkaMelding(metadata, JSONObject(komplettSøknad.somJson()), Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER)
        } catch (exception: Exception){
            logger.info("Feilet ved å legge melding på Kafka.")
            logger.info("Fjerner hold på persisterte vedlegg")
            vedleggService.fjernHoldPåPersistertVedlegg(søknad.vedlegg, callId, søker.somDokumentEier())
        }
    }
}