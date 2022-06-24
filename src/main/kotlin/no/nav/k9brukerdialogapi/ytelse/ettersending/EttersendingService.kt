package no.nav.k9brukerdialogapi.ytelse.ettersending

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
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.KomplettSøknad
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknad
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(EttersendingService::class.java)

class EttersendingService(
    private val kafkaProdusent: KafkaProducer,
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService
) {

    suspend fun registrer(søknad: Søknad, callId: CallId, metadata: Metadata, idToken: IdToken) {
        logger.info(formaterStatuslogging(Ytelse.ETTERSENDING, søknad.søknadId, "registreres."))

        søknad.valider()
        val søker = søkerService.hentSøker(idToken, callId).also { søker -> søker.valider() }

        val dokumentEier = søker.somDokumentEier()
        val vedlegg = vedleggService.hentVedlegg(søknad.vedlegg, idToken, callId).also { vedlegg ->
            vedlegg.valider("vedlegg", søknad.vedlegg)
        }

        vedleggService.persisterVedlegg(søknad.vedlegg, callId, dokumentEier)

        val komplettSøknad = søknad.somKomplettSøknad(søker, søknad.somK9Format(søker), vedlegg.map { it.title })

        try {
            leggMeldingPåKafka(metadata, komplettSøknad)
        } catch (e: Exception) {
            håndterKafkaFeil(søknad, callId, dokumentEier)
        }
    }

    private fun leggMeldingPåKafka(metadata: Metadata, komplettSøknad: KomplettSøknad) {
        kafkaProdusent.produserKafkaMelding(
            metadata,
            JSONObject(komplettSøknad.somJson()),
            Ytelse.ETTERSENDING
        )
    }

    private suspend fun håndterKafkaFeil(søknad: Søknad, callId: CallId, dokumentEier: DokumentEier) {
        logger.info("Feilet ved å legge melding på Kafka. Fjerner hold på persisterte vedlegg")
        vedleggService.fjernHoldPåPersistertVedlegg(søknad.vedlegg, callId, dokumentEier)
        throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
    }
}