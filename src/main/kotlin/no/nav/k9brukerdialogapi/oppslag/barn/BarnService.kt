package no.nav.k9brukerdialogapi.oppslag.barn

import com.github.benmanes.caffeine.cache.Cache
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.oppslag.TilgangNektetException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BarnService(
    private val barnGateway: BarnGateway,
    private val cache: Cache<String, List<BarnOppslag>>
) {
    private val logger: Logger = LoggerFactory.getLogger(BarnService::class.java)

    internal suspend fun hentBarn(
        idToken: IdToken,
        callId: CallId
    ): List<BarnOppslag> {
        var barnFraCache = cache.getIfPresent(idToken.getNorskIdentifikasjonsnummer())
        if (barnFraCache != null) return barnFraCache

        return try {
            val barn = barnGateway.hentBarn(
                idToken = idToken,
                callId = callId
            ).map { it.tilBarnOppslag() }

            cache.put(idToken.getNorskIdentifikasjonsnummer(), barn)
            barn
        } catch (cause: Throwable) {
            when (cause) {
                is TilgangNektetException -> throw cause
                else -> {
                    logger.error("Feil ved henting av barn, returnerer en tom liste", cause)
                    emptyList()
                }
            }
        }
    }
}
