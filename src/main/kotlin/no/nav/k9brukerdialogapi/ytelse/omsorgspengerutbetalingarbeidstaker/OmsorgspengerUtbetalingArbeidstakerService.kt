package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Søknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OmsorgspengerUtbetalingArbeidstakerService(
    private val søkerService: SøkerService
) {
    private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtbetalingArbeidstakerService::class.java)
    private val YTELSE = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId
    ){
        logger.info(formaterStatuslogging(YTELSE, søknad.søknadId, "registreres."))
        val søker = søkerService.hentSøker(idToken, callId)
        søker.valider()
    }
}