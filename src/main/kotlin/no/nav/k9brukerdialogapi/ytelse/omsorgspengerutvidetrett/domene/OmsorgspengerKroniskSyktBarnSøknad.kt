package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarnSøknadValidator
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.utils.StringUtils.FRITEKST_REGEX
import no.nav.k9brukerdialogapi.utils.StringUtils.FritekstPattern
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
    internal val legeerklæring: List<URL> = listOf(),
    internal val samværsavtale: List<URL>? = null,
    private val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    private val kroniskEllerFunksjonshemming: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val sammeAdresse: BarnSammeAdresse?,
    private val høyereRisikoForFravær: Boolean? = null,
    private val høyereRisikoForFraværBeskrivelse: String? = null, // skal valideres hvis høyereRisikoForFravær er true
    private val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (barn.manglerIdentifikator()) barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
    }

    override fun somK9Format(søker: Søker, metadata: Metadata): K9Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            OmsorgspengerKroniskSyktBarn(
                barn.somK9Barn(),
                kroniskEllerFunksjonshemming
            )
                .medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as OmsorgspengerKroniskSyktBarn
        ).medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    fun byggK9DataBruktTilUtledning(metadata: Metadata): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): OmsorgspengerKroniskSyktBarnKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerKroniskSyktBarnKomplettSøknad(
            språk = språk,
            søknadId = søknadId,
            mottatt = mottatt,
            kroniskEllerFunksjonshemming = kroniskEllerFunksjonshemming,
            søker = søker,
            barn = barn,
            relasjonTilBarnet = relasjonTilBarnet,
            sammeAdresse = sammeAdresse,
            høyereRisikoForFravær = høyereRisikoForFravær,
            høyereRisikoForFraværBeskrivelse = høyereRisikoForFraværBeskrivelse,
            legeerklæringVedleggId = legeerklæring.map { it.vedleggId() },
            samværsavtaleVedleggId = samværsavtale?.map { it.vedleggId() } ?: listOf(),
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            k9FormatSøknad = k9Format as no.nav.k9.søknad.Søknad
        )
    }

    override fun valider() = mutableListOf<String>().apply {
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        kreverIkkeNull(sammeAdresse, "sammeAdresse må være satt.")

        if (sammeAdresse != null && sammeAdresse == BarnSammeAdresse.NEI) {
            krever(samværsavtale?.isNotEmpty(), "Dersom sammeAdresse er NEI kan ikke samværsavtale være tom.")
        }

        if (høyereRisikoForFravær == true) {
            krever(
                !høyereRisikoForFraværBeskrivelse.isNullOrBlank(),
                "høyereRisikoForFraværBeskrivelse må være satt når høyereRisikoForFravær er true"
            )
            if (!høyereRisikoForFraværBeskrivelse.isNullOrBlank()) {
                krever(
                    høyereRisikoForFraværBeskrivelse.length in 1..1000,
                    "høyereRisikoForFraværBeskrivelse må være mellom 1 og 1000 tegn"
                )
                krever(
                    FRITEKST_REGEX.matches(høyereRisikoForFraværBeskrivelse),
                    "høyereRisikoForFraværBeskrivelse matcher ikke tilatt møønster: $FritekstPattern"
                )
            }
        }

        addAll(barn.valider("barn"))

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> =
        OmsorgspengerKroniskSyktBarnSøknadValidator()

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(legeerklæring)
        samværsavtale?.let { addAll(it) }
    }
}
