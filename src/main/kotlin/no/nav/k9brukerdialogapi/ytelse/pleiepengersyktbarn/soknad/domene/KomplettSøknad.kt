package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.innsending.KomplettInnsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSøknad(
    val apiDataVersjon: String? = null,
    val språk: Språk? = null,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val søker: Søker,
    val barn: BarnDetaljer,
    val arbeidsgivere: List<Arbeidsgiver>,
    var vedleggId : List<String> = listOf(),
    val fødselsattestVedleggId: List<String>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val nattevåk: Nattevåk?,
    val beredskap: Beredskap?,
    val frilans: Frilans,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9FormatSøknad: Søknad? = null
): KomplettInnsending
