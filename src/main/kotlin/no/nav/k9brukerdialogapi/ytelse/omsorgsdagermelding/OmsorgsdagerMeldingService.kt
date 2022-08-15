package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding

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
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Melding
import org.json.JSONObject
import org.slf4j.LoggerFactory

class OmsorgsdagerMeldingService(
    private val søkerService: SøkerService,
    private val barnService: BarnService,
    private val kafkaProducer: KafkaProducer,
    private val vedleggService: VedleggService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    internal suspend fun registrer(melding: Melding, callId: CallId, metadata: Metadata, idToken: IdToken) {
        logger.info(formaterStatuslogging(melding.type.somYtelse(), melding.søknadId, "registreres."))

        val søker = søkerService.hentSøker(idToken, callId).also { it.valider() }

        melding.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId))
        melding.valider()

        if(melding.inneholderVedlegg()) validerOgPersisterVedlegg(melding, idToken, callId, søker.somDokumentEier())

        try {
            kafkaProducer.produserKafkaMelding(metadata, JSONObject(melding.somKomplettMelding(søker).somJson()), melding.type.somYtelse())
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka. $exception")
            if(melding.inneholderVedlegg()) fjernHoldPåPersisterteVedlegg(melding, callId, søker.somDokumentEier())
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    private suspend fun fjernHoldPåPersisterteVedlegg(melding: Melding, callId: CallId, dokumentEier: DokumentEier) {
        melding.fordeling?.samværsavtale?.let {
            logger.info("Fjerner hold på persisterte vedlegg")
            vedleggService.fjernHoldPåPersistertVedlegg(it, callId, dokumentEier)
        }
    }

    private suspend fun validerOgPersisterVedlegg(melding: Melding, idToken: IdToken, callId: CallId, dokumentEier: DokumentEier) {
        melding.fordeling?.samværsavtale?.let {
            logger.info("Validerer vedlegg")
            vedleggService
                .hentVedlegg(it, idToken, callId)
                .valider("fordeling.samværsavtale", it)

            logger.info("Persisterer vedlegg")
            vedleggService.persisterVedlegg(it, callId, dokumentEier)
        }
    }
}