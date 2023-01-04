package no.nav.k9brukerdialogapi.kafka

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
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

    @Test
    fun `forvent feil dersom KafkaConfig opprettes me ugyldig trustStore path`(
        @MockK trustStore: Pair<String, String>,
    ) {
        every { trustStore.first } throws NullPointerException("something went wrong")
        org.junit.jupiter.api.assertThrows<NullPointerException> {
            KafkaConfig(
                bootstrapServers = "localhost:9092",
                trustStore = trustStore,
                keyStore = null,
                transactionalId = "transactionalId"
            )
        }
    }

    @Test
    fun `forvent feil dersom KafkaConfig opprettes me ugyldig keyStore path`(
        @MockK keyStore: Pair<String, String>,
    ) {
        every { keyStore.first } throws NullPointerException("something went wrong")
        org.junit.jupiter.api.assertThrows<NullPointerException> {
            KafkaConfig(
                bootstrapServers = "localhost:9092",
                trustStore = null,
                keyStore = keyStore,
                transactionalId = "transactionalId"
            )
        }
    }
}
