package no.nav.k9brukerdialogapi

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getTokendingsWellKnownUrl
import no.nav.k9brukerdialogapi.wiremock.getK9BrukerdialogCacheUrl
import no.nav.k9brukerdialogapi.wiremock.getK9MellomlagringUrl
import no.nav.k9brukerdialogapi.wiremock.getK9OppslagUrl
import no.nav.k9brukerdialogapi.wiremock.getSifInnsynApiUrl
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.testcontainers.containers.KafkaContainer

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        kafkaEnvironment: KafkaContainer? = null,
        port: Int = 8080,
        k9OppslagUrl: String? = wireMockServer?.getK9OppslagUrl(),
        sifInnaynApiUrl: String? = wireMockServer?.getSifInnsynApiUrl(),
        k9MellomlagringUrl: String? = wireMockServer?.getK9MellomlagringUrl(),
        k9BrukerdialogCacheUrl: String? = wireMockServer?.getK9BrukerdialogCacheUrl(),
        corsAdresses: String = "http://localhost:8080",
        mockOAuth2Server: MockOAuth2Server
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.authorization.cookie_name", "selvbetjening-idtoken"),
            Pair("nav.gateways.k9_oppslag_url","$k9OppslagUrl"),
            Pair("nav.gateways.k9_mellomlagring_url", "$k9MellomlagringUrl"),
            Pair("nav.gateways.k9_brukerdialog_cache_url", "$k9BrukerdialogCacheUrl"),
            Pair("nav.gateways.sif_innsyn_api_url", "$sifInnaynApiUrl"),
            Pair("nav.cors.addresses", corsAdresses),
        )

        if (wireMockServer != null) {
            // Clients
            map["nav.auth.clients.0.alias"] = "azure-v2"
            map["nav.auth.clients.0.client_id"] = "k9-brukerdialog-api"
            map["nav.auth.clients.0.private_key_jwk"] = ClientCredentials.ClientC.privateKeyJwk
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()

            map["nav.auth.clients.1.alias"] = "tokenx"
            map["nav.auth.clients.1.client_id"] = "k9-brukerdialog-api"
            map["nav.auth.clients.1.private_key_jwk"] = ClientCredentials.ClientC.privateKeyJwk
            map["nav.auth.clients.1.discovery_endpoint"] = wireMockServer.getTokendingsWellKnownUrl()

            // Issuers
            map["no.nav.security.jwt.issuers.0.issuer_name"] = "tokendings"
            map["no.nav.security.jwt.issuers.0.discoveryurl"] = "${mockOAuth2Server.wellKnownUrl("tokendings")}"
            map["no.nav.security.jwt.issuers.0.accepted_audience"] = "dev-gcp:dusseldorf:k9-brukerdialog-api"

            // scopes
            map["nav.auth.scopes.k9-mellomlagring-client-id"] = "k9-mellomlagring-client-id/.default"
            map["nav.auth.scopes.k9_mellomlagring_tokenx_audience"] = "dev-gcp:dusseldorf:k9-mellomlagring"
            map["nav.auth.scopes.k9_selvbetjening_oppslag_tokenx_audience"] = "dev-fss:dusseldorf:k9-selvbetjening-oppslag"
            map["nav.auth.scopes.sif_innsyn_api_tokenx_audience"] = "dev-gcp:dusseldorf:sif-innsyn-api"
            map["nav.auth.scopes.k9-brukerdialog-cache-tokenx-audience"] = "dev-gcp:dusseldorf:k9-brukerdialog-cache"
        }

        map["nav.mellomlagring.søknad_tid_timer"] = "1"
        map["nav.cache.innsending.expiry_in_seconds"] = "0"

        // Kafka
        kafkaEnvironment?.let {
            map["nav.kafka.bootstrap_servers"] = it.bootstrapServers
            map["nav.kafka.transactionalId"] = "k9-brukerdialog-api"

        }

        return map.toMap()
    }
}
