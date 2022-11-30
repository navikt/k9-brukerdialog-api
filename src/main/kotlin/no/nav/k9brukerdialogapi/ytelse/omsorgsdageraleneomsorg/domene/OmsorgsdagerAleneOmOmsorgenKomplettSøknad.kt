package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.innsending.KomplettInnsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.ZonedDateTime

class OmsorgsdagerAleneOmOmsorgenKomplettSøknad(
    private val søknadId: String,
    private val mottatt: ZonedDateTime,
    private val søker: Søker,
    private val språk: String,
    private val barn: Barn,
    private val k9Søknad: Søknad,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
): KomplettInnsending {
    override fun equals(other: Any?) = this === other || (other is OmsorgsdagerAleneOmOmsorgenKomplettSøknad && this.equals(other))

    private fun equals(other: OmsorgsdagerAleneOmOmsorgenKomplettSøknad) =
        this.søknadId == other.søknadId &&
        this.barn == other.barn &&
        this.k9Søknad.søknadId == other.k9Søknad.søknadId
}
