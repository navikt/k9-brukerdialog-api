package no.nav.k9brukerdialogapi.innsending

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.ytelse.pls.v1.PleiepengerLivetsSluttfaseSøknadValidator
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
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InnsendingService(
    private val søkerService: SøkerService,
    private val kafkaProdusent: KafkaProducer,
    private val vedleggService: VedleggService,
) {

    internal suspend fun registrer(innsending: Innsending, callId: CallId, idToken: IdToken, metadata: Metadata) {
        val ytelse = innsending.ytelse()
        logger.info(formaterStatuslogging(ytelse, innsending.søknadId(), "registreres."))

        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()
        val dokumentEier = søker.somDokumentEier()

        innsending.valider()
        val k9Format = innsending.somK9Format(søker)
        validerK9Format(innsending, k9Format)

        if (innsending.inneholderVedlegg()) {
            validerVedlegg(innsending, idToken, callId)
            persisterVedlegg(innsending, callId, dokumentEier)
        }

        try {
            kafkaProdusent.produserKafkaMelding(
                metadata = metadata, ytelse = ytelse,
                komplettSøknadSomJson = JSONObject(innsending.somKomplettSøknad(søker, k9Format).somJson())
            )
        } catch (e: Exception) {
            logger.info("Feilet med å legge søknad på Kafka.")
            if (innsending.inneholderVedlegg()) fjernHoldPåPersisterteVedlegg(innsending, callId, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet med å legge søknad på Kafka.")
        }
    }

    fun validerK9Format(innsending: Innsending, søknad: K9Søknad) {
        val feil = innsending.validator().valider(søknad).map {
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "K9-format feilkode: ${it.feilkode}"
            )
        }.toMutableSet()

        if (feil.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(feil))
        }
    }

    private suspend fun validerVedlegg(innsending: Innsending, idToken: IdToken, callId: CallId) {
        logger.info("Validerer vedlegg")
        vedleggService.hentVedlegg(innsending.vedlegg(), idToken, callId).valider("vedlegg", innsending.vedlegg())
    }

    private suspend fun persisterVedlegg(innsending: Innsending, callId: CallId, eier: DokumentEier) {
        logger.info("Persisterer vedlegg")
        vedleggService.persisterVedlegg(innsending.vedlegg(), callId, eier)
    }

    private suspend fun fjernHoldPåPersisterteVedlegg(innsending: Innsending, callId: CallId, eier: DokumentEier) {
        if (innsending.inneholderVedlegg()) {
            logger.info("Fjerner hold på persisterte vedleggU")
            vedleggService.fjernHoldPåPersistertVedlegg(innsending.vedlegg(), callId, eier)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
