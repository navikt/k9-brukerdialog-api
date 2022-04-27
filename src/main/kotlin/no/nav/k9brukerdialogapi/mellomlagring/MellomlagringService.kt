package no.nav.k9brukerdialogapi.mellomlagring

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import java.time.ZoneOffset
import java.time.ZonedDateTime

class MellomlagringService(
    private val mellomlagretTidTimer: String,
    private val k9BrukerdialogCacheGateway: K9BrukerdialogCacheGateway
) {
    private fun genererNøkkelPrefix(ytelse: Ytelse) = "mellomlagring_$ytelse"

    suspend fun hentMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse
    ) = k9BrukerdialogCacheGateway.hentMellomlagretSøknad(
        nøkkelPrefiks = genererNøkkelPrefix(ytelse),
        idToken = idToken,
        callId = callId
    )?.verdi

    suspend fun settMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse,
        verdi: String,
    ) = k9BrukerdialogCacheGateway.mellomlagreSøknad(
        cacheRequest = CacheRequest(
            nøkkelPrefiks = genererNøkkelPrefix(ytelse),
            verdi = verdi,
            utløpsdato = ZonedDateTime.now(ZoneOffset.UTC).plusHours(mellomlagretTidTimer.toLong()),
            opprettet = ZonedDateTime.now(ZoneOffset.UTC),
            endret = null
        ),
        idToken = idToken,
        callId = callId
    )

    suspend fun slettMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse
    ) = k9BrukerdialogCacheGateway.slettMellomlagretSøknad(genererNøkkelPrefix(ytelse), idToken, callId)

    suspend fun oppdaterMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse,
        verdi: String,
    ): CacheResponse {
        val eksisterendeMellomlagring = k9BrukerdialogCacheGateway.hentMellomlagretSøknad(
            nøkkelPrefiks = genererNøkkelPrefix(ytelse),
            idToken = idToken,
            callId = callId
        )
        return if(eksisterendeMellomlagring != null){
            k9BrukerdialogCacheGateway.oppdaterMellomlagretSøknad(
                cacheRequest = CacheRequest(
                    nøkkelPrefiks = genererNøkkelPrefix(ytelse),
                    verdi = verdi,
                    utløpsdato = eksisterendeMellomlagring.utløpsdato,
                    opprettet = eksisterendeMellomlagring.opprettet,
                    endret = ZonedDateTime.now()

                ),
                idToken = idToken,
                callId = callId

            )
        } else settMellomlagring(callId, idToken, ytelse, verdi)
    }
}