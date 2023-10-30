package no.nav.k9brukerdialogapi.oppslag.søker

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.ytelse.Ytelse

class SøkerService (
    private val søkerGateway: SøkerGateway
) {
    suspend fun hentSøker(
        idToken: IdToken,
        callId: CallId,
        ytelse: Ytelse
    ): Søker {
        return søkerGateway.hentSøker(idToken, callId, ytelse)
    }
}
