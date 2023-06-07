package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAleneSøknadValidator
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class OmsorgspengerMdlertidigAleneSøknad(
    val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val id: String,
    private val språk: String,
    private val annenForelder: AnnenForelder,
    private val barn: List<Barn> = listOf(),
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val dataBruktTilUtledning: MutableMap<String, Any>? = null
): Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    override fun somK9Format(søker: Søker, metadata: Metadata): no.nav.k9.søknad.Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            OmsorgspengerMidlertidigAlene(
                barn.map { it.somK9Barn() },
                annenForelder.somK9AnnenForelder(),
                null
            ).medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as OmsorgspengerMidlertidigAlene
        ).medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    fun byggK9DataBruktTilUtledning(metadata: Metadata): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .setAnnetData(dataBruktTilUtledning)

    override fun somKomplettSøknad(søker: Søker, k9Format: no.nav.k9.søknad.Innsending?, titler: List<String>): OmsorgspengerMdlertidigAleneKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerMdlertidigAleneKomplettSøknad(
            mottatt = mottatt,
            søker = søker,
            søknadId = søknadId,
            id = id,
            språk = språk,
            annenForelder = annenForelder,
            barn = barn,
            k9Format = k9Format as no.nav.k9.søknad.Søknad,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { barn ->
            if (barn.manglerIdentifikator()) barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        }
    }

    override fun valider() = mutableListOf<String>().apply {
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(barn.isNotEmpty(), "Listen over barn kan ikke være tom")
        addAll(annenForelder.valider("annenForelder"))
        barn.forEachIndexed { index, barn -> addAll(barn.valider("barn[$index]")) }

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = listOf()
    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerMidlertidigAleneSøknadValidator()
}
