package no.nav.k9brukerdialogapi.innsending

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
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
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InnsendingService(
    private val søkerService: SøkerService,
    private val kafkaProdusent: KafkaProducer,
    private val vedleggService: VedleggService,
) {

    internal suspend fun registrer(innsending: Innsending, callId: CallId, idToken: IdToken, metadata: Metadata) {
        val søker = søkerService.hentSøker(idToken, callId)

        logger.info(formaterStatuslogging(innsending.ytelse(), innsending.søknadId(), "registreres."))

        søker.valider()

        innsending.valider()
        val k9Format = innsending.somK9Format(søker, metadata)
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
            val komplettInnsending = innsending.somKomplettSøknad(søker, k9Format)
            kafkaProdusent.produserKafkaMelding(
                metadata,
                JSONObject(komplettInnsending.somJson()),
                innsending.ytelse()
            )
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.", exception)
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
            logger.error("Feilet ved å legge melding på Kafka.", exception)
            logger.info("Fjerner hold på persisterte vedlegg")
            fjernHoldPåPersisterteVedlegg(innsending, callId, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    fun validerK9Format(
        innsending: Innsending,
        k9Format: no.nav.k9.søknad.Innsending
    ) {
        val feil = when (k9Format) {
            is Søknad -> {
                when (innsending.ytelse()) {
                    Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN -> {
                        requireNotNull(innsending.gyldigeEndringsPerioder()) {
                            "GyldigeEndringsPerioder kan ikke være null for ${Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN}"
                        }
                        val søknadValidator = innsending.søknadValidator() as PleiepengerSyktBarnSøknadValidator
                        søknadValidator.valider(k9Format, innsending.gyldigeEndringsPerioder())
                    }

                    else -> {
                        innsending.søknadValidator()?.valider(k9Format)
                    }
                }
            }

            is Ettersendelse -> innsending.ettersendelseValidator()?.valider(k9Format)
            else -> null
        }?.map {
            logger.error("${it.felt} feilet pga. ${it.feilkode}")
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "K9-format valideringsfeil"
            )
        }?.toMutableSet()

        if (!feil.isNullOrEmpty()) {
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
