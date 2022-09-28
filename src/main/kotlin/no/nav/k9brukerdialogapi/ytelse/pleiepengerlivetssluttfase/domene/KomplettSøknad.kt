package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.LocalDate
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

class KomplettSøknad(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate,
    private val mottatt: ZonedDateTime,
    private val vedleggId: List<String>,
    private val opplastetIdVedleggId: List<String>,
    private val medlemskap: Medlemskap,
    private val pleietrengende: Pleietrengende,
    private val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    private val ferieuttakIPerioden: FerieuttakIPerioden?,
    private val frilans: Frilans?,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    private val utenlandskNæring: List<UtenlandskNæring>,
    private val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    private val harVærtEllerErVernepliktig: Boolean?,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val k9Format: K9Søknad
){
    override fun equals(other: Any?) = this === other || other is KomplettSøknad && this.equals(other)
    private fun equals(other: KomplettSøknad) = søknadId == other.søknadId
}