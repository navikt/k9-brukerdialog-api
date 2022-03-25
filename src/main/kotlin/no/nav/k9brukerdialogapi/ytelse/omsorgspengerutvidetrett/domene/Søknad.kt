package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

private val k9FormatVersjon = Versjon.of("1.0.0")

class Søknad(
    private val språk: String,
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val søknadId: String = UUID.randomUUID().toString(),
    private var barn: Barn,
    private val sammeAdresse: Boolean?,
    val legeerklæring: List<URL> = listOf(),
    val samværsavtale: List<URL>? = null,
    private val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    private val kroniskEllerFunksjonshemming: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {
    fun barnManglerIdentifikator() = barn.manglerIdentifikator()

    fun leggTilIdentifikatorPåBarn(barnFraOppslag: List<BarnOppslag>) = barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)

    fun tilK9Format(søker: Søker): K9Søknad = K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.somK9Søker(),
        OmsorgspengerKroniskSyktBarn(
            barn.somK9Barn(),
            kroniskEllerFunksjonshemming
        )
    )

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

    fun valider() = mutableSetOf<Violation>().apply {
        addAll(barn.valider())

        if(sammeAdresse != null && !sammeAdresse && samværsavtale.isNullOrEmpty()){
            add(
                Violation(
                    parameterName = "sammeAdresse og samværsavtale",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom sammeAdresse er false kan ikke samværsavtale være null eller tom.",
                    invalidValue = "sammeAdresse=$sammeAdresse, samværsavtale=$samværsavtale"

                )
            )
        }

        if (!harBekreftetOpplysninger) {
            add(
                Violation(
                    parameterName = "harBekreftetOpplysninger",
                    parameterType = ParameterType.ENTITY,
                    reason = "Opplysningene må bekreftes for å sende inn søknad."
                )
            )
        }

        if (!harForståttRettigheterOgPlikter) {
            add(
                Violation(
                    parameterName = "harForståttRettigheterOgPlikter",
                    parameterType = ParameterType.ENTITY,
                    reason = "Må ha forstått rettigheter og plikter for å sende inn søknad."
                )
            )
        }

        if (this.isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }
}

enum class SøkerBarnRelasjon() {
    @JsonAlias("mor") MOR,
    @JsonAlias("far") FAR,
    @JsonAlias("fosterforelder") FOSTERFORELDER,
    @JsonAlias("adoptivforelder") ADOPTIVFORELDER
}