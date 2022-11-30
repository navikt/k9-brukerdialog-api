package no.nav.k9brukerdialogapi.ytelse.ettersending.domene

import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9brukerdialogapi.innsending.KomplettInnsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.ZonedDateTime

class KomplettEttersendelse(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val mottatt: ZonedDateTime,
    private val vedleggId: List<String>,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val beskrivelse: String?,
    private val søknadstype: Søknadstype,
    private val titler: List<String>,
    private val k9Format: Ettersendelse
): KomplettInnsending {

    override fun equals(other: Any?) = this === other || (other is KomplettEttersendelse && this.equals(other))

    private fun equals(other: KomplettEttersendelse) = this.søknadId == other.søknadId
            && this.k9Format.søknadId == other.k9Format.søknadId
            && this.titler == other.titler

}
