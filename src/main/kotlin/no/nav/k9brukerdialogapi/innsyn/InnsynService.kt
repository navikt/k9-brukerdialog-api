package no.nav.k9brukerdialogapi.innsyn

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.endringsmelding.domene.Endringsmelding

class InnsynService(
    private val innsynGateway: InnsynGateway
) {

    suspend fun hentSøknadsopplysningerForBarn(
        idToken: IdToken,
        callId: CallId,
        endringsmelding: Endringsmelding
    ): K9SakInnsynSøknad {
        return innsynGateway.hentSøknadsopplysninger(idToken, callId)
            .firstOrNull { k9SakInnsynSøknad: K9SakInnsynSøknad ->
                k9SakInnsynSøknad.barn.identitetsnummer == endringsmelding.ytelse.barn.personIdent.verdi
            } ?: throw IllegalStateException("Søknadsopplysninger inneholdt ikke riktig barn.")
    }
}
