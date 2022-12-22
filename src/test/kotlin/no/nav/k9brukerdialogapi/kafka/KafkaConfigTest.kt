package no.nav.k9brukerdialogapi.kafka

import org.junit.jupiter.api.Test

class KafkaConfigTest {

    @Test
    fun `forvent ingen feil dersom KafkaConfig opprettes med trustStore og keyStore`() {
        org.junit.jupiter.api.assertDoesNotThrow {
            KafkaConfig(
                bootstrapServers = "localhost:9092",
                trustStore = Pair("src/test/resources/truststore.jks", "password"),
                keyStore = Pair("src/test/resources/keystore.p12", "password"),
                transactionalId = "transactionalId"
            )
        }
    }

    @Test
    fun `forvent ingen feil dersom KafkaConfig opprettes uten trustStore og keyStore`() {
        org.junit.jupiter.api.assertDoesNotThrow {
            KafkaConfig(
                bootstrapServers = "localhost:9092",
                trustStore = null,
                keyStore = null,
                transactionalId = "transactionalId"
            )
        }
    }
}
