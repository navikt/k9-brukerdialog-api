package no.nav.k9brukerdialogapi

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.k9brukerdialogapi.kafka.KafkaConfig
import java.net.URI
import java.time.Duration

data class Configuration(val config : ApplicationConfig) {

    internal fun getCookieName(): String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    internal fun getWhitelistedCorsAddreses() : List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    internal fun getK9BrukerdialogCacheUrl() = URI(config.getRequiredString("nav.gateways.k9_brukerdialog_cache_url", secret = false))
    internal fun getK9BrukerdialogCacheTokenxAudience() = getScopesFor("k9-brukerdialog-cache-tokenx-audience")
    internal fun getSoknadMellomlagringTidTimer() = config.getRequiredString("nav.mellomlagring.søknad_tid_timer", false)

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))
    internal fun getK9SelvbetjeningOppslagTokenxAudience(): Set<String> = getScopesFor("k9_selvbetjening_oppslag_tokenx_audience")

    internal fun getK9MellomlagringUrl() = URI(config.getRequiredString("nav.gateways.k9_mellomlagring_url", secret = false))
    internal fun getK9MellomlagringScopes() = getScopesFor("k9-mellomlagring-client-id")
    internal fun getK9MellomlagringTokenxAudience(): Set<String> = getScopesFor("k9_mellomlagring_tokenx_audience")

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()

    internal fun getKafkaConfig() = config.getRequiredString("nav.kafka.bootstrap_servers", secret = false).let { bootstrapServers ->
        val trustStore =
            config.getOptionalString("nav.kafka.truststore_path", secret = false)?.let { trustStorePath ->
                config.getOptionalString("nav.kafka.credstore_password", secret = true)?.let { credstorePassword ->
                    Pair(trustStorePath, credstorePassword)
                }
            }

        val keyStore = config.getOptionalString("nav.kafka.keystore_path", secret = false)?.let { keystorePath ->
            config.getOptionalString("nav.kafka.credstore_password", secret = true)?.let { credstorePassword ->
                Pair(keystorePath, credstorePassword)
            }
        }

        val transactionalId = config.getRequiredString("nav.kafka.transactionalId", false)

        KafkaConfig(
            bootstrapServers = bootstrapServers,
            trustStore = trustStore,
            keyStore = keyStore,
            transactionalId = transactionalId
        )
    }

    internal fun<K, V>cache(
        expiry: Duration = Duration.ofMinutes(config.getRequiredString("nav.cache.barn.expiry_in_minutes", secret = false).toLong())
    ) : Cache<K, V> {
        val maxSize = config.getRequiredString("nav.cache.barn.max_size", secret = false).toLong()
        return Caffeine.newBuilder()
            .expireAfterWrite(expiry)
            .maximumSize(maxSize)
            .build()
    }
}
