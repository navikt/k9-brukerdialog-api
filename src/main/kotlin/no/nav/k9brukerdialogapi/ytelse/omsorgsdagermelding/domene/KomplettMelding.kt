package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.ZonedDateTime

class KomplettMelding(
    private val søknadId: String,
    private val søker: Søker,
    private val mottatt: ZonedDateTime,
    private val id: String,
    private val språk: String,
    private val barn: List<Barn>,
    private val mottakerFnr: String,
    private val mottakerNavn: String,
    private val harAleneomsorg: Boolean,
    private val harUtvidetRett: Boolean,
    private val erYrkesaktiv: Boolean,
    private val arbeiderINorge: Boolean,
    private val arbeidssituasjon: List<Arbeidssituasjon>,
    private val antallDagerBruktIÅr: Int? = null,
    internal val type: Meldingstype,
    private val korona: Koronaoverføre? = null,
    private val overføring: Overføre? = null,
    private val fordeling: KomplettFordele? = null,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {
    override fun equals(other: Any?) = this === other || (other is KomplettMelding && this.equals(other))

    private fun equals(other: KomplettMelding) = this.søknadId == other.søknadId && this.id == other.id
}