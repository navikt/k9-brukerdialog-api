package no.nav.k9brukerdialogapi

import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.kafka.TopicEntry
import no.nav.k9brukerdialogapi.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogapi.kafka.hentTopicForYtelse
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.json.JSONObject
import java.time.Duration
import kotlin.test.assertEquals

private const val username = "srvkafkaclient"
private const val password = "kafkaclient"

object KafkaWrapper {
    fun bootstrap() : KafkaEnvironment {
        val kafkaEnvironment = KafkaEnvironment(
            users = listOf(JAASCredential(username, password)),
            autoStart = true,
            withSchemaRegistry = false,
            withSecurity = true,
            topicNames= listOf(
                OMSORGSPENGER_UTVIDET_RETT_TOPIC,
                OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC,
                ETTERSENDING_TOPIC,
                OMSORGSDAGER_ALENEOMSORG_TOPIC
            )
        )
        return kafkaEnvironment
    }
}

private fun KafkaEnvironment.testConsumerProperties() : MutableMap<String, Any>?  {
    return HashMap<String, Any>().apply {
        put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokersURL)
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
        put(SaslConfigs.SASL_MECHANISM, "PLAIN")
        put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";")
        put(ConsumerConfig.GROUP_ID_CONFIG, "k9-brukerdialogapi")
    }
}

internal fun KafkaEnvironment.testConsumer() : KafkaConsumer<String, TopicEntry<JSONObject>> {
    val consumer = KafkaConsumer(
        testConsumerProperties(),
        StringDeserializer(),
        OutgoingDeserialiser()
    )
    consumer.subscribe(listOf(
        OMSORGSPENGER_UTVIDET_RETT_TOPIC,
        OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC,
        ETTERSENDING_TOPIC,
        OMSORGSDAGER_ALENEOMSORG_TOPIC
    ))
    return consumer
}


internal fun KafkaConsumer<String, TopicEntry<JSONObject>>.hentSøknad(
    søknadId: String,
    ytelse: Ytelse,
    maxWaitInSeconds: Long = 20,
) : TopicEntry<JSONObject> {
    val end = System.currentTimeMillis() + Duration.ofSeconds(maxWaitInSeconds).toMillis()
    while (System.currentTimeMillis() < end) {
        seekToBeginning(assignment())
        val entries = poll(Duration.ofSeconds(1))
            .records(hentTopicForYtelse(ytelse))
            .filter { it.key() == søknadId }

        if (entries.isNotEmpty()) {
            assertEquals(1, entries.size)
            return entries.first().value()
        }
    }
    throw IllegalStateException("Fant ikke opprettet oppgave for melding med søknadsId $søknadId etter $maxWaitInSeconds sekunder.")
}

private class OutgoingDeserialiser : Deserializer<TopicEntry<JSONObject>> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<JSONObject> {
        val json = JSONObject(String(data))
        val metadata = json.getJSONObject("metadata")
        return TopicEntry(
            metadata = Metadata(
                version = metadata.getInt("version"),
                correlationId = metadata.getString("correlationId")
            ),
            data = json.getJSONObject("data")
        )
    }
    override fun close() {}
}