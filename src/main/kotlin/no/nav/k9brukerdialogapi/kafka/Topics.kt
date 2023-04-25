package no.nav.k9brukerdialogapi.kafka

import no.nav.k9brukerdialogapi.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTBETALING_SNF_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN
import no.nav.k9brukerdialogapi.ytelse.Ytelse.ETTERSENDING
import no.nav.k9brukerdialogapi.ytelse.Ytelse.ETTERSENDING_OMP
import no.nav.k9brukerdialogapi.ytelse.Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE
import no.nav.k9brukerdialogapi.ytelse.Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSDAGER_ALENEOMSORG
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTBETALING_SNF
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTVIDET_RETT
import no.nav.k9brukerdialogapi.ytelse.Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE
import no.nav.k9brukerdialogapi.ytelse.Ytelse.PLEIEPENGER_SYKT_BARN
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject

object Topics {
    const val OMSORGSPENGER_UTVIDET_RETT_TOPIC = "dusseldorf.omp-utv-kronisk-sykt-barn-soknad-mottatt"
    const val OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC = "dusseldorf.privat-omsorgspenger-midlertidig-alene-mottatt"
    const val ETTERSENDING_TOPIC = "dusseldorf.ettersendelse-mottatt"
    const val OMSORGSDAGER_ALENEOMSORG_TOPIC = "dusseldorf.privat-omsorgsdager-aleneomsorg-mottatt"
    const val OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC = "dusseldorf.omp-utbetaling-at-soknad-mottatt"
    const val OMSORGSPENGER_UTBETALING_SNF_TOPIC = "dusseldorf.privat-omsorgspengerutbetalingsoknad-mottatt-v2"
    const val PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC = "dusseldorf.pp-i-livets-sluttfase-soknad-mottatt"
    const val PLEIEPENGER_SYKT_BARN_TOPIC = "dusseldorf.pp-sykt-barn-soknad-mottatt"
    const val MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC =
        "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-mottatt"
}

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V,
)

internal fun hentTopicForYtelse(ytelse: Ytelse) = when (ytelse) {
    PLEIEPENGER_LIVETS_SLUTTFASE -> PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
    OMSORGSPENGER_UTVIDET_RETT -> OMSORGSPENGER_UTVIDET_RETT_TOPIC
    OMSORGSPENGER_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
    ETTERSENDING, ETTERSENDING_OMP, ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE, ETTERSENDING_PLEIEPENGER_SYKT_BARN -> ETTERSENDING_TOPIC
    OMSORGSDAGER_ALENEOMSORG -> OMSORGSDAGER_ALENEOMSORG_TOPIC
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
    OMSORGSPENGER_UTBETALING_SNF -> OMSORGSPENGER_UTBETALING_SNF_TOPIC
    PLEIEPENGER_SYKT_BARN -> PLEIEPENGER_SYKT_BARN_TOPIC
    ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN -> MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
}

internal class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
    override fun serialize(topic: String, data: TopicEntry<JSONObject>): ByteArray {
        val metadata = JSONObject()
            .put("correlationId", data.metadata.correlationId)
            .put("version", data.metadata.version)
            .put("soknadDialogCommitSha", data.metadata.soknadDialogCommitSha)

        return JSONObject()
            .put("metadata", metadata)
            .put("data", data.data)
            .toString()
            .toByteArray()
    }
}
