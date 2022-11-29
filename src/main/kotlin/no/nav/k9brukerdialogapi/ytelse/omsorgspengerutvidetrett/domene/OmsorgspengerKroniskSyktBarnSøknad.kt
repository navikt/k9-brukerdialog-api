package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarnSøknadValidator
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class OmsorgspengerKroniskSyktBarnSøknad(
    val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private var barn: Barn,
    private val sammeAdresse: Boolean?,
    internal val legeerklæring: List<URL> = listOf(),
    internal val samværsavtale: List<URL>? = null,
    private val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    private val kroniskEllerFunksjonshemming: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
): Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (barn.manglerIdentifikator()) barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
    }

    override fun somK9Format(søker: Søker): K9Søknad = K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.somK9Søker(),
        OmsorgspengerKroniskSyktBarn(
            barn.somK9Barn(),
            kroniskEllerFunksjonshemming
        )
    )

    override fun somKomplettSøknad(søker: Søker, k9Format: K9Søknad) = OmsorgspengerKroniskSyktBarnKomplettSøknad(
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

    override fun valider() = mutableListOf<String>().apply {
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        kreverIkkeNull(sammeAdresse, "sammeAdresse må være satt.")
        if(sammeAdresse != null && !sammeAdresse){
            krever(samværsavtale?.isNotEmpty(), "Dersom sammeAdresse er false kan ikke samværsavtale være tom.")
        }
        addAll(barn.valider("barn"))

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    override fun validator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerKroniskSyktBarnSøknadValidator()
    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(legeerklæring)
        samværsavtale?.let { addAll(it) }
    }
}
