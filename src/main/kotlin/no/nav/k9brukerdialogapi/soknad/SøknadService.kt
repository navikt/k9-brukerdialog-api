package no.nav.k9brukerdialogapi.soknad

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
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PilsSøknad
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.validerK9FormatPILS
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SøknadService(
    private val søkerService: SøkerService,
    private val kafkaProdusent: KafkaProducer,
    private val vedleggService: VedleggService,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val YTELSE = Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE

    internal suspend fun registrer(søknad: Søknad, callId: CallId, idToken: IdToken, metadata: Metadata) {
        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()

        when (val ytelse = søknad.ytelse()) {
            Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE -> {
                søknad as PilsSøknad
                logger.info(formaterStatuslogging(ytelse, søknad.søknadId, "registreres."))

                val k9Format = søknad.somK9Format(søker)
                validerK9FormatPILS(k9Format)
                søknad.valider()

                val dokumentEier = søker.somDokumentEier()
                if (søknad.inneholderVedlegg()) {
                    validerVedlegg(søknad, idToken, callId)
                    persisterVedlegg(søknad, callId, dokumentEier)
                }

                try {
                    kafkaProdusent.produserKafkaMelding(
                        metadata = metadata, ytelse = YTELSE,
                        komplettSøknadSomJson = JSONObject(søknad.somKomplettSøknad(søker, k9Format).somJson())
                    )
                } catch (e: Exception) {
                    logger.info("Feilet med å legge søknad på Kafka.")
                    if (søknad.inneholderVedlegg()) fjernHoldPåPersisterteVedlegg(søknad, callId, dokumentEier)
                    throw MeldingRegistreringFeiletException("Feilet med å legge søknad på Kafka.")
                }
            }
            Ytelse.OMSORGSPENGER_UTVIDET_RETT -> TODO()
            Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> TODO()
            Ytelse.ETTERSENDING -> TODO()
            Ytelse.OMSORGSDAGER_ALENEOMSORG -> TODO()
            Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> TODO()
            Ytelse.OMSORGSPENGER_UTBETALING_SNF -> TODO()
            Ytelse.OMSORGSDAGER_MELDING -> TODO()
            Ytelse.OMSORGSDAGER_MELDING_FORDELING -> TODO()
            Ytelse.OMSORGSDAGER_MELDING_OVERFORING -> TODO()
            Ytelse.OMSORGSDAGER_MELDING_KORONAOVERFORING -> TODO()
            Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN -> TODO()
            Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE -> TODO()
            Ytelse.ETTERSENDING_OMP -> TODO()
        }
    }

    private suspend fun validerVedlegg(pilsSøknad: PilsSøknad, idToken: IdToken, callId: CallId) {
        if (pilsSøknad.vedleggUrls.isNotEmpty()) {
            logger.info("Validerer vedleggUrls")
            vedleggService.hentVedlegg(pilsSøknad.vedleggUrls, idToken, callId)
                .valider("vedleggUrls", pilsSøknad.vedleggUrls)
        }

        if (pilsSøknad.opplastetIdVedleggUrls.isNotEmpty()) {
            logger.info("Validerer opplastetIdVedleggUrls")
            vedleggService.hentVedlegg(pilsSøknad.opplastetIdVedleggUrls, idToken, callId)
                .valider("opplastetIdVedleggUrls", pilsSøknad.opplastetIdVedleggUrls)
        }
    }

    private suspend fun persisterVedlegg(pilsSøknad: PilsSøknad, callId: CallId, eier: DokumentEier) {
        if (pilsSøknad.vedleggUrls.isNotEmpty()) {
            logger.info("Persisterer vedleggUrls")
            vedleggService.persisterVedlegg(pilsSøknad.vedleggUrls, callId, eier)
        }

        if (pilsSøknad.opplastetIdVedleggUrls.isNotEmpty()) {
            logger.info("Persisterer opplastetIdVedleggUrls")
            vedleggService.persisterVedlegg(pilsSøknad.opplastetIdVedleggUrls, callId, eier)
        }
    }

    private suspend fun fjernHoldPåPersisterteVedlegg(pilsSøknad: PilsSøknad, callId: CallId, eier: DokumentEier) {
        if (pilsSøknad.vedleggUrls.isNotEmpty()) {
            logger.info("Fjerner hold på persisterte vedleggUrls")
            vedleggService.fjernHoldPåPersistertVedlegg(pilsSøknad.vedleggUrls, callId, eier)
        }

        if (pilsSøknad.opplastetIdVedleggUrls.isNotEmpty()) {
            logger.info("Fjerner hold på persisterte opplastetIdVedleggUrls")
            vedleggService.fjernHoldPåPersistertVedlegg(pilsSøknad.opplastetIdVedleggUrls, callId, eier)
        }
    }
}
