package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import java.time.ZonedDateTime

class KomplettSøknad(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val søknadId: String,
    val id: String,
    val språk: String,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
){
    override fun equals(other: Any?) = this === other || (other is KomplettSøknad && this.equals(other))

    private fun equals(other: KomplettSøknad) =
            this.id == other.id &&
            this.søknadId == other.søknadId &&
            this.k9Format.søknadId == other.k9Format.søknadId
}