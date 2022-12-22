package no.nav.k9brukerdialogapi.utils

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import org.slf4j.Logger

object LoggingUtils {
    fun Logger.logTokenExchange(idToken: IdToken, exchangeToken: IdToken) {
        info("Utvekslet token fra {} med token fra {}.", idToken.issuer(), exchangeToken.issuer())
    }
}
