package no.nav.k9brukerdialogapi.kafka

import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.json.JSONObject

object Topics {
    const val OMSORGSPENGER_UTVIDET_RETT_TOPIC = "dusseldorf.privat-omsorgspengesoknad-mottatt-v2"
    const val OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC = "dusseldorf.privat-omsorgspenger-midlertidig-alene-mottatt"
}

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V
)

data class TopicUse<V>(
    val name: String,
    val valueSerializer : Serializer<TopicEntry<V>>
) {
    internal fun keySerializer() = StringSerializer()
}

internal fun hentTopicUseForYtelse(ytelse: Ytelse) = when(ytelse){
    Ytelse.OMSORGSPENGER_UTVIDET_RETT -> OMSORGSPENGER_UTVIDET_RETT_TOPIC_USE
    Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC_USE
}

internal val OMSORGSPENGER_UTVIDET_RETT_TOPIC_USE = TopicUse(
    name = Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC,
    valueSerializer = SøknadSerializer()
)

internal val OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC_USE = TopicUse(
    name = Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC,
    valueSerializer = SøknadSerializer()
)

private class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
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