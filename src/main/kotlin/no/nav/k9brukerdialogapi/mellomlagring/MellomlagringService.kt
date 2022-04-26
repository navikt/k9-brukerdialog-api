package no.nav.k9brukerdialogapi.mellomlagring

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.ytelse.Ytelse

class MellomlagringService(
    private val mellomlagretTidTimer: String,
    private val k9BrukerdialogCacheGateway: K9BrukerdialogCacheGateway
) {
    suspend fun getMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse
    ) = k9BrukerdialogCacheGateway.hentMellomlagretSøknad(
        nøkkelPrefiks = genererNøkkelPrefix(ytelse),
        idToken = idToken,
        callId = callId
    )?.verdi

    suspend fun setMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse,
        verdi: String,
    ): CacheResponse {
        val cacheRequest = CacheRequest.genererCacheRequest(verdi, mellomlagretTidTimer.toLong(), ytelse)
        return k9BrukerdialogCacheGateway.mellomlagreSøknad(
            cacheRequest = cacheRequest,
            idToken = idToken,
            callId = callId
        )
    }

    suspend fun deleteMellomlagring(
        callId: CallId,
        idToken: IdToken,
        ytelse: Ytelse
    ) = k9BrukerdialogCacheGateway.slettMellomlagretSøknad(
        genererNøkkelPrefix(ytelse), idToken, callId
    )

    suspend fun updateMellomlagring(
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
                cacheRequest = CacheRequest.genererCacheRequest(verdi, mellomlagretTidTimer.toLong(), ytelse),
                idToken = idToken,
                callId = callId

            )
        } else {
            setMellomlagring(callId, idToken, ytelse, verdi)
        }

    }
}