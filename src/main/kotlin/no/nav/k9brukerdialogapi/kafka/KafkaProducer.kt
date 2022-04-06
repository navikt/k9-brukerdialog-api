package no.nav.k9brukerdialogapi.kafka

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogapi.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.json.JSONObject
import org.slf4j.LoggerFactory

class KafkaProducer(
    val kafkaConfig: KafkaConfig
) : HealthCheck {
    private val NAME = "KafkaProducer"
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)
    private val produsent = KafkaProducer(
        kafkaConfig.producer(NAME),
        StringSerializer(),
        SøknadSerializer()
    )

    init {
        produsent.initTransactions()
    }

    private fun beginTransaction() = produsent.beginTransaction()
    private fun abortTransaction() = produsent.abortTransaction()
    private fun commitTransaction() = produsent.commitTransaction()
    internal fun close() = produsent.close()

    internal fun produserKafkaMelding(metadata: Metadata, komplettSøknadSomJson: JSONObject, ytelse: Ytelse) {
        beginTransaction()
        leggMeldingPåTopic(metadata, komplettSøknadSomJson, hentTopicForYtelse(ytelse), ytelse)
        commitTransaction()
    }

    internal fun produserKafkaMeldinger(metadata: Metadata, komplettSøknadSomJson: List<JSONObject>, ytelse: Ytelse){
        try {
            beginTransaction()
            komplettSøknadSomJson.forEach { leggMeldingPåTopic(metadata, it, hentTopicForYtelse(ytelse), ytelse) }
            commitTransaction()
        } catch (e: Exception) {
            logger.info("Feilet med produsering av kafkamelding")
            abortTransaction()
            throw e
        }
    }

    private fun leggMeldingPåTopic(metadata: Metadata, komplettSøknadSomJson: JSONObject, topic: String, ytelse: Ytelse) {
        val recordMetaData = produsent.send(
            ProducerRecord(
                topic,
                komplettSøknadSomJson.getString("søknadId"),
                TopicEntry(
                    metadata = metadata,
                    data = komplettSøknadSomJson
                )
            )
        ).get()
        logger.info(formaterStatuslogging(ytelse, komplettSøknadSomJson.getString("søknadId"), "sendes til topic ${topic} med offset '${recordMetaData.offset()}' til partition '${recordMetaData.partition()}'"))
    }

    override suspend fun check(): Result { // Håndtere slik at man gir bedring tilbakemelding på spesifikk topic som feiler
        return try {
            produsent.partitionsFor(OMSORGSPENGER_UTVIDET_RETT_TOPIC)
            produsent.partitionsFor(OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC)
            produsent.partitionsFor(ETTERSENDING_TOPIC)
            Healthy(NAME, "Tilkobling til Kafka OK!")
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            UnHealthy(NAME, "Feil ved tilkobling mot Kafka. ${cause.message}")
        }
    }
}