package no.nav.k9brukerdialogapi.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(KafkaConfig::class.java)
private const val ID_PREFIX = "k9-brukerdialog-api"

class KafkaConfig(
    bootstrapServers: String,
    trustStore: Pair<String, String>?,
    keyStore: Pair<String, String>?,
    transactionalId: String
) {
    private val producer = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
        put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
        medTrustStore(trustStore)
        medKeyStore(keyStore)
    }

    internal fun producer(name: String) = producer.apply {
        put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
        put(ProducerConfig.CLIENT_ID_CONFIG, "$ID_PREFIX$name")
    }

}

private fun Properties.medTrustStore(trustStore: Pair<String, String>?) {
    trustStore?.let {
        try {
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(it.first).absolutePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, it.second)
            logger.info("Truststore på '${it.first}' konfigurert.")
        } catch (cause: Throwable) {
            logger.error("Feilet for konfigurering av truststore på '${it.first}'", cause)
        }
    }
}

private fun Properties.medKeyStore(keyStore: Pair<String, String>?) {
    keyStore?.let {
        try {
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, File(it.first).absolutePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, it.second)
            logger.info("Keystore på '${it.first}' konfigurert.")
        } catch (cause: Throwable) {
            logger.error("Feilet for konfigurering av keystore på '${it.first}'", cause)
        }
    }
}
