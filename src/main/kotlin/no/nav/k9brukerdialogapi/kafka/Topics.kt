package no.nav.k9brukerdialogapi.kafka

import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject

object Topics {
    const val OMSORGSPENGER_UTVIDET_RETT_TOPIC = "dusseldorf.privat-omsorgspengesoknad-mottatt-v2"
    const val OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC = "dusseldorf.privat-omsorgspenger-midlertidig-alene-mottatt"
}

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V
)

internal fun hentTopicForYtelse(ytelse: Ytelse) = when(ytelse){
    Ytelse.OMSORGSPENGER_UTVIDET_RETT -> Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
    Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
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