package no.nav.k9brukerdialogapi.oppslag.søker

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId

class SøkerService (
    private val søkerGateway: SøkerGateway
) {
    suspend fun hentSøker(
        idToken: IdToken,
        callId: CallId
    ): Søker {
        return søkerGateway.hentSøker(idToken, callId)
    }
}