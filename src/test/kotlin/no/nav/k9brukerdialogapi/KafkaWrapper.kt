package no.nav.k9brukerdialogapi

import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.kafka.TopicEntry
import no.nav.k9brukerdialogapi.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTBETALING_SNF_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.k9brukerdialogapi.kafka.hentTopicForYtelse
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.json.JSONObject
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.test.assertEquals

private lateinit var kafkaContainer: KafkaContainer
private const val confluentVersion = "7.2.1"

object KafkaWrapper {
    fun bootstrap(): KafkaContainer {
        kafkaContainer = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:$confluentVersion")
        )
        kafkaContainer.start()
        kafkaContainer.createTopicsForTest()
        return kafkaContainer
    }
}

private fun KafkaContainer.createTopicsForTest() {
    // Dette er en workaround for att testcontainers (pr. versjon 1.17.5) ikke håndterer autocreate topics
    AdminClient.create(testProducerProperties("admin")).createTopics(
        listOf(
            NewTopic(OMSORGSPENGER_UTVIDET_RETT_TOPIC, 1, 1),
            NewTopic(OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC, 1, 1),
            NewTopic(OMSORGSDAGER_ALENEOMSORG_TOPIC, 1, 1),
            NewTopic(OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC, 1, 1),
            NewTopic(OMSORGSPENGER_UTBETALING_SNF_TOPIC, 1, 1),
            NewTopic(PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC, 1, 1),
            NewTopic(ETTERSENDING_TOPIC, 1, 1),
            NewTopic(PLEIEPENGER_SYKT_BARN_TOPIC, 1, 1),
            NewTopic(MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC, 1, 1)
        )
    )
}

private fun KafkaContainer.testProducerProperties(clientId: String): MutableMap<String, Any>? {
    return HashMap<String, Any>().apply {
        put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.CLIENT_ID_CONFIG, clientId)
    }
}

private fun KafkaContainer.testConsumerProperties() : MutableMap<String, Any>?  {
    return HashMap<String, Any>().apply {
        put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, "k9-brukerdialogapi")
    }
}

internal fun KafkaContainer.testConsumer() : KafkaConsumer<String, TopicEntry<JSONObject>> {
    val consumer = KafkaConsumer(
        testConsumerProperties(),
        StringDeserializer(),
        OutgoingDeserialiser()
    )
    consumer.subscribe(listOf(
        OMSORGSPENGER_UTVIDET_RETT_TOPIC,
        OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC,
        ETTERSENDING_TOPIC,
        OMSORGSDAGER_ALENEOMSORG_TOPIC,
        OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC,
        OMSORGSPENGER_UTBETALING_SNF_TOPIC,
        PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC,
        PLEIEPENGER_SYKT_BARN_TOPIC,
        MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
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
