package no.nav.k9brukerdialogapi.kafka

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTVIDET_RETT_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.KomplettSøknad
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject
import org.slf4j.LoggerFactory

class KafkaProducer(
    val kafkaConfig: KafkaConfig
) : HealthCheck {
    private val NAME = "KafkaProducer"
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    private val OMSORGSPENGER_UTVIDET_RETT_TOPIC = TopicUse(
        name = Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC,
        valueSerializer = SøknadSerializer()
    )
    private val producer = KafkaProducer(
        kafkaConfig.producer(NAME),
        OMSORGSPENGER_UTVIDET_RETT_TOPIC.keySerializer(),
        OMSORGSPENGER_UTVIDET_RETT_TOPIC.valueSerializer
    )

    internal fun produserKafkaMelding(metadata: Metadata, komplettSøknadSomJson: JSONObject, ytelse: Ytelse) {
        if (metadata.version != 1) throw IllegalStateException("Kan ikke legge melding med versjon ${metadata.version} til prosessering.")

        val topic = when(ytelse){
            Ytelse.OMSORGSPENGER_UTVIDET_RETT -> OMSORGSPENGER_UTVIDET_RETT_TOPIC
        }

        val recordMetaData = producer.send(
            ProducerRecord(
                topic.name,
                komplettSøknadSomJson.getString("søknadId"),
                TopicEntry(
                    metadata = metadata,
                    data = komplettSøknadSomJson
                )
            )
        ).get()
        logger.info(formaterStatuslogging(ytelse, komplettSøknadSomJson.getString("søknadId"), "sendes til topic ${topic.name} med offset '${recordMetaData.offset()}' til partition '${recordMetaData.partition()}'"))
    }

    internal fun stop() = producer.close()

    override suspend fun check(): Result { // Håndtere slik at man gir bedring tilbakemelding på spesifikk topic som feiler
        return try {
            producer.partitionsFor(OMSORGSPENGER_UTVIDET_RETT_TOPIC.name)
            Healthy(NAME, "Tilkobling til Kafka OK!")
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            UnHealthy(NAME, "Feil ved tilkobling mot Kafka. ${cause.message}")
        }
    }
}

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