package no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class KomplettSøknad(
    val språk: String,
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val søknadId: String = UUID.randomUUID().toString(),
    var barn: Barn,
    val søker: Søker,
    val sammeAdresse: Boolean?,
    var legeerklæringVedleggId: List<String>,
    var samværsavtaleVedleggId: List<String>,
    val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    val kroniskEllerFunksjonshemming: Boolean,
    val k9FormatSøknad: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)