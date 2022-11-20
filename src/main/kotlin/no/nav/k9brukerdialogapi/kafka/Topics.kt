package no.nav.k9brukerdialogapi.kafka

import no.nav.k9brukerdialogapi.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSDAGER_MELDING_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTBETALING_SNF_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OPPLAERINGSPENGER_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.Ytelse.*
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject

object Topics {
    const val OMSORGSPENGER_UTVIDET_RETT_TOPIC = "dusseldorf.privat-omsorgspengesoknad-mottatt-v2"
    const val OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC = "dusseldorf.privat-omsorgspenger-midlertidig-alene-mottatt"
    const val ETTERSENDING_TOPIC = "dusseldorf.privat-k9-ettersending-mottatt-v2"
    const val OMSORGSDAGER_ALENEOMSORG_TOPIC = "dusseldorf.privat-omsorgsdager-aleneomsorg-mottatt"
    const val OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC = "dusseldorf.privat-omsorgspengerutbetalingsoknad-arbeidstaker-mottatt-v2"
    const val OMSORGSPENGER_UTBETALING_SNF_TOPIC = "dusseldorf.privat-omsorgspengerutbetalingsoknad-mottatt-v2"
    const val OMSORGSDAGER_MELDING_TOPIC = "dusseldorf.privat-omsorgsdager-melding-mottatt"
    const val PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC = "dusseldorf.privat-pp-livets-sluttfase-mottatt"
    const val OPPLAERINGSPENGER_TOPIC = "dusseldorf.opplaeringspenger-mottatt"
}

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V
)

internal fun hentTopicForYtelse(ytelse: Ytelse) = when(ytelse){
    PLEIEPENGER_LIVETS_SLUTTFASE -> PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
    OMSORGSPENGER_UTVIDET_RETT -> OMSORGSPENGER_UTVIDET_RETT_TOPIC
    OMSORGSPENGER_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
    ETTERSENDING -> ETTERSENDING_TOPIC
    OMSORGSDAGER_ALENEOMSORG -> OMSORGSDAGER_ALENEOMSORG_TOPIC
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
    OMSORGSPENGER_UTBETALING_SNF -> OMSORGSPENGER_UTBETALING_SNF_TOPIC
    OMSORGSDAGER_MELDING, OMSORGSDAGER_MELDING_FORDELING, OMSORGSDAGER_MELDING_OVERFORING, OMSORGSDAGER_MELDING_KORONAOVERFORING -> OMSORGSDAGER_MELDING_TOPIC
    OPPLAERINGSPENGER -> OPPLAERINGSPENGER_TOPIC
}

internal class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
    override fun serialize(topic: String, data: TopicEntry<JSONObject>): ByteArray {
        val metadata = JSONObject()
            .put("correlationId", data.metadata.correlationId)
            .put("version", data.metadata.version)

        return JSONObject()
            .put("metadata", metadata)
            .put("data", data.data)
            .toString()
            .toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}