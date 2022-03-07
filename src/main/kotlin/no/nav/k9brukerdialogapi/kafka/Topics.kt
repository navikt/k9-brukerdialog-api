package no.nav.k9brukerdialogapi.kafka

import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V
)

internal data class TopicUse<V>(
    val name: String,
    val valueSerializer : Serializer<TopicEntry<V>>
) {
    internal fun keySerializer() = StringSerializer()
}

object Topics {
    const val MOTTATT_ETTERSENDING_TOPIC = "dusseldorf.privat-k9-ettersending-mottatt-v2"
    const val OMSORGSPENGER_UTVIDET_RETT_TOPIC = "dusseldorf.privat-omsorgspengesoknad-mottatt-v2"
}