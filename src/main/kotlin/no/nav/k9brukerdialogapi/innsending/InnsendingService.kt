package no.nav.k9brukerdialogapi.innsending

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.MeldingRegistreringFeiletException
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import no.nav.k9brukerdialogapi.vedlegg.Vedlegg
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.valider
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InnsendingService(
    private val søkerService: SøkerService,
    private val kafkaProdusent: KafkaProducer,
    private val vedleggService: VedleggService
) {

    internal suspend fun registrer(innsending: Innsending, callId: CallId, idToken: IdToken, metadata: Metadata) {
        val søker = søkerService.hentSøker(idToken, callId)

        logger.info(formaterStatuslogging(innsending.ytelse(), innsending.søknadId(), "registreres."))

        søker.valider()

        innsending.valider()
        val k9Format = innsending.somK9Format(søker)
        k9Format?.let { validerK9Format(innsending, it) }

        if (innsending.inneholderVedlegg()) registrerSøknadMedVedlegg(
            innsending,
            idToken,
            callId,
            søker,
            k9Format,
            metadata
        )
        else registrerSøknadUtenVedlegg(innsending, søker, k9Format, metadata)
    }

    private fun registrerSøknadUtenVedlegg(
        innsending: Innsending,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        metadata: Metadata,
    ) {
        try {
            kafkaProdusent.produserKafkaMelding(
                metadata,
                JSONObject(innsending.somKomplettSøknad(søker, k9Format).somJson()),
                innsending.ytelse()
            )
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.")
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    private suspend fun registrerSøknadMedVedlegg(
        innsending: Innsending,
        idToken: IdToken,
        callId: CallId,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        metadata: Metadata,
    ) {
        logger.info("Validerer ${innsending.vedlegg().size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(innsending.vedlegg(), idToken, callId)
        validerVedlegg(innsending, vedlegg)

        val dokumentEier = søker.somDokumentEier()
        persisterVedlegg(innsending, callId, dokumentEier)

        try {
            kafkaProdusent.produserKafkaMelding(
                metadata,
                JSONObject(innsending.somKomplettSøknad(søker, k9Format, vedlegg.map { it.title }).somJson()),
                innsending.ytelse()
            )
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.")
            logger.info("Fjerner hold på persisterte vedlegg")
            fjernHoldPåPersisterteVedlegg(innsending, callId, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    fun validerK9Format(innsending: Innsending, k9Format: no.nav.k9.søknad.Innsending) {
        val feil = when (k9Format) {
            is Søknad -> innsending.søknadValidator()?.valider(k9Format)
            is Ettersendelse -> innsending.ettersendelseValidator()?.valider(k9Format)
            else -> null
        }?.map {
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "K9-format feilkode: ${it.feilkode}"
            )
        }?.toMutableSet()

        if (feil != null && feil.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(feil))
        }
    }

    private fun validerVedlegg(innsending: Innsending, vedlegg: List<Vedlegg>) {
        logger.info("Validerer vedlegg")
        vedlegg.valider("vedlegg", innsending.vedlegg())
    }

    private suspend fun persisterVedlegg(innsending: Innsending, callId: CallId, eier: DokumentEier) {
        logger.info("Persisterer vedlegg")
        vedleggService.persisterVedlegg(innsending.vedlegg(), callId, eier)
    }

    private suspend fun fjernHoldPåPersisterteVedlegg(innsending: Innsending, callId: CallId, eier: DokumentEier) {
        if (innsending.inneholderVedlegg()) {
            logger.info("Fjerner hold på persisterte vedlegg")
            vedleggService.fjernHoldPåPersistertVedlegg(innsending.vedlegg(), callId, eier)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
