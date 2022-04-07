package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.ZonedDateTime

class KomplettSøknad(
    private val søknadId: String,
    private val mottatt: ZonedDateTime,
    private val søker: Søker,
    private val språk: String,
    private val barn: Barn,
    private val k9Søknad: Søknad,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
){
    override fun equals(other: Any?) = this === other || (other is KomplettSøknad && this.equals(other))

    private fun equals(other: KomplettSøknad) =
        this.søknadId == other.søknadId &&
        this.barn == other.barn &&
        this.k9Søknad.søknadId == other.k9Søknad.søknadId
}