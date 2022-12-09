package no.nav.k9brukerdialogapi

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.auth.IdTokenStatusPages
import no.nav.helse.dusseldorf.ktor.auth.clients
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.k9brukerdialogapi.general.AccessTokenClientResolver
import no.nav.k9brukerdialogapi.innsending.InnsendingCache
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.mellomlagring.K9BrukerdialogCacheGateway
import no.nav.k9brukerdialogapi.mellomlagring.MellomlagringService
import no.nav.k9brukerdialogapi.mellomlagring.mellomlagringApis
import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.ArbeidsgiverGateway
import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.ArbeidsgiverService
import no.nav.k9brukerdialogapi.oppslag.barn.BarnGateway
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.oppslagRoutes
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerGateway
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.vedlegg.K9MellomlagringGateway
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.vedlegg.vedleggApis
import no.nav.k9brukerdialogapi.ytelse.Ytelse.*
import no.nav.k9brukerdialogapi.ytelse.ytelseRoutes
import no.nav.security.token.support.v2.RequiredClaims
import no.nav.security.token.support.v2.asIssuerProps
import no.nav.security.token.support.v2.tokenValidationSupport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.k9BrukerdialogApi() {
    val logger: Logger = LoggerFactory.getLogger("nav.k9BrukerdialogApi")
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    System.setProperty("dusseldorf.ktor.serializeProblemDetailsWithContentNegotiation", "true")

    val configuration = Configuration(environment.config)
    val config = this.environment.config
    val allIssuers = config.asIssuerProps().keys

    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients())
    val tokenxClient = CachedAccessTokenClient(accessTokenClientResolver.tokenxClient)

    val k9MellomlagringGateway = K9MellomlagringGateway(
            baseUrl = configuration.getK9MellomlagringUrl(),
            accessTokenClient = accessTokenClientResolver.azureV2AccessTokenClient,
            exchangeTokenClient = tokenxClient,
            k9MellomlagringScope = configuration.getK9MellomlagringScopes(),
            k9MellomlagringTokenxAudience = configuration.getK9MellomlagringTokenxAudience()
    )

    val vedleggService = VedleggService(
            k9MellomlagringGateway
    )

    val søkerService = SøkerService(
            SøkerGateway(
                    baseUrl = configuration.getK9OppslagUrl(),
                    accessTokenClient = tokenxClient,
                    k9SelvbetjeningOppslagTokenxAudience = configuration.getK9SelvbetjeningOppslagTokenxAudience()
            )
    )

    val barnService = BarnService(
            barnGateway = BarnGateway(
                    baseUrl = configuration.getK9OppslagUrl(),
                    accessTokenClient = tokenxClient,
                    k9SelvbetjeningOppslagTokenxAudience = configuration.getK9SelvbetjeningOppslagTokenxAudience()
            ),
            cache = configuration.cache()
    )

    val arbeidsgiverService = ArbeidsgiverService(
            ArbeidsgiverGateway(
                    baseUrl = configuration.getK9OppslagUrl(),
                    accessTokenClient = tokenxClient,
                    k9SelvbetjeningOppslagTokenxAudience = configuration.getK9SelvbetjeningOppslagTokenxAudience()
            )
    )

    val kafkaProducer = KafkaProducer(configuration.getKafkaConfig())

    val k9BrukerdialogCacheGateway = K9BrukerdialogCacheGateway(
            tokenxClient = tokenxClient,
            k9BrukerdialogCacheTokenxAudience = configuration.getK9BrukerdialogCacheTokenxAudience(),
            baseUrl = configuration.getK9BrukerdialogCacheUrl()
    )

    val mellomlagringService = MellomlagringService(
            mellomlagretTidTimer = configuration.getSoknadMellomlagringTidTimer(),
            k9BrukerdialogCacheGateway = k9BrukerdialogCacheGateway
    )

    val innsendingCache = InnsendingCache(expireSeconds = configuration.getInnSendingCacheExpiryInSeconds())

    val innsendingService = InnsendingService(søkerService, kafkaProducer, vedleggService)

    install(ContentNegotiation) {
        jackson {
            dusseldorfConfigured()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        allowCredentials = true
        logger.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            logger.info("Adding host {} with scheme {}", it.host, it.scheme)
            allowHost(host = it.authority, schemes = listOf(it.scheme))
        }
    }

    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())

    install(Authentication) {
        allIssuers.forEach { issuer: String ->
            tokenValidationSupport(
                    name = issuer,
                    config = config,
                    requiredClaims = RequiredClaims(
                            issuer = issuer,
                            claimMap = arrayOf("acr=Level4")
                    )
            )
        }
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        IdTokenStatusPages()
    }

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")

        authenticate(*allIssuers.toTypedArray()) {
            ytelseRoutes(
                    idTokenProvider = idTokenProvider,
                    barnService = barnService,
                    innsendingService = innsendingService,
                    innsendingCache = innsendingCache
            )

            oppslagRoutes(
                    idTokenProvider = idTokenProvider,
                    søkerService = søkerService,
                    barnservice = barnService,
                    arbeidsgiverService = arbeidsgiverService
            )

            vedleggApis(
                    vedleggService = vedleggService,
                    idTokenProvider = idTokenProvider
            )

            mellomlagringApis(
                    mellomlagringService = mellomlagringService,
                    idTokenProvider = idTokenProvider
            )
        }

        val healthService = HealthService(
                healthChecks = setOf(
                        kafkaProducer,
                        HttpRequestHealthCheck(
                                mapOf(
                                        Url.buildURL(
                                                baseUrl = configuration.getK9MellomlagringUrl(),
                                                pathParts = listOf("health")
                                        ) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK),
                                        Url.buildURL(
                                                baseUrl = configuration.getK9BrukerdialogCacheUrl(),
                                                pathParts = listOf("actuator", "health")
                                        ) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK)
                                )
                        )
                )
        )

        HealthReporter(
                app = appId,
                healthService = healthService,
                frequency = Duration.ofMinutes(1)
        )

        DefaultProbeRoutes()
        MetricsRoute()
        HealthRoute(
                healthService = healthService
        )
    }

    environment.monitor.subscribe(ApplicationStopping) {
        logger.info("Stopper Kafka Producer.")
        kafkaProducer.close()
        logger.info("Kafka Producer Stoppet.")
    }

    install(MicrometerMetrics) {
        init(appId)
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        call.request.log()
    }

    install(CallId) {
        generated()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
        mdc("id_token_jti") { call ->
            try {
                val idToken = idTokenProvider.getIdToken(call)
                logger.info("Issuer [{}]", idToken.issuer())
                idToken.getId()
            } catch (cause: Throwable) {
                null
            }
        }
        mdc("ytelse") { call ->
            val request = call.request.uri
            when {
                request.contains(OMSORGSPENGER_UTVIDET_RETT_URL.removePrefix("/")) -> OMSORGSPENGER_UTVIDET_RETT.name.lowercase()
                request.contains(OMSORGSPENGER_MIDLERTIDIG_ALENE_URL.removePrefix("/")) -> OMSORGSPENGER_MIDLERTIDIG_ALENE.name.lowercase()
                request.contains(ETTERSENDING_URL.removePrefix("/")) -> ETTERSENDING.name.lowercase()
                request.contains(OMSORGSDAGER_ALENEOMSORG_URL.removePrefix("/")) -> OMSORGSDAGER_ALENEOMSORG.name.lowercase()
                request.contains(OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL.removePrefix("/")) -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER.name.lowercase()
                request.contains(OMSORGSPENGER_UTBETALING_SNF_URL.removePrefix("/")) -> OMSORGSPENGER_UTBETALING_SNF.name.lowercase()
                request.contains(OMSORGSDAGER_MELDING_FORDELING_URL.removePrefix("/")) -> OMSORGSDAGER_MELDING_FORDELING.name.lowercase()
                request.contains(OMSORGSDAGER_MELDING_OVERFORING_URL.removePrefix("/")) -> OMSORGSDAGER_MELDING_OVERFORING.name.lowercase()
                request.contains(OMSORGSDAGER_MELDING_KORONAOVERFORING_URL.removePrefix("/")) -> OMSORGSDAGER_MELDING_KORONAOVERFORING.name.lowercase()
                request.contains(OMSORGSDAGER_MELDING.name) -> OMSORGSDAGER_MELDING.name.lowercase()
                request.contains(PLEIEPENGER_LIVETS_SLUTTFASE_URL.removePrefix("/")) -> PLEIEPENGER_LIVETS_SLUTTFASE.name.lowercase()
                else -> null
            }
        }
    }
}

internal fun ObjectMapper.k9BrukerdialogKonfiguert() = dusseldorfConfigured().apply {
    configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
}

internal fun ObjectMapper.k9SelvbetjeningOppslagKonfigurert(): ObjectMapper {
    return dusseldorfConfigured().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerModule(JavaTimeModule())
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }
}
