package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.barn.hentNorskIdentifikatorForBarn
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val språk: String,
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val søknadId: String = UUID.randomUUID().toString(),
    var barn: Barn,
    val sammeAdresse: Boolean?,
    val legeerklæring: List<URL> = listOf(),
    val samværsavtale: List<URL>? = null,
    val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    val kroniskEllerFunksjonshemming: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    infix fun oppdaterBarnsNorskIdentifikatorFra(listeOverBarnOppslag: List<BarnOppslag>) {
        if(barn.manglerNorskIdentifikator()){
            barn oppdaterNorskIdentifikatorMed listeOverBarnOppslag.hentNorskIdentifikatorForBarn(barn.aktørId)
        }
    }

    fun tilKomplettSøknad(søker: Søker, k9Format: no.nav.k9.søknad.Søknad) = KomplettSøknad(
        språk = språk,
        søknadId = søknadId,
        mottatt = mottatt,
        kroniskEllerFunksjonshemming = kroniskEllerFunksjonshemming,
        søker = søker,
        barn = barn,
        relasjonTilBarnet = relasjonTilBarnet,
        sammeAdresse = sammeAdresse,
        legeerklæringVedleggId = legeerklæring.map { it.vedleggId() },
        samværsavtaleVedleggId = samværsavtale?.map { it.vedleggId() } ?: listOf(),
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = harBekreftetOpplysninger,
        k9FormatSøknad = k9Format
    )
}

enum class SøkerBarnRelasjon() {
    @JsonAlias("mor") MOR(),
    @JsonAlias("far") FAR(),
    @JsonAlias("adoptivforelder") ADOPTIVFORELDER(),
    @JsonAlias("fosterforelder") FOSTERFORELDER()
}

