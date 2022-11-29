package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.innsending.KomplettInnsending
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Barn.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Meldingstype.*
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class OmsorgsdagerMelding(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val id: String,
    private val språk: String,
    private val barn: List<Barn>,
    private val mottakerFnr: String,
    private val mottakerNavn: String,
    private val harAleneomsorg: Boolean? = null,
    private val harUtvidetRett: Boolean? = null,
    private val erYrkesaktiv: Boolean? = null,
    private val arbeiderINorge: Boolean? = null,
    private val arbeidssituasjon: List<Arbeidssituasjon>,
    private val antallDagerBruktIÅr: Int? = null,
    internal val type: Meldingstype,
    private val korona: Koronaoverføre? = null,
    private val overføring: Overføre? = null,
    internal val fordeling: Fordele? = null,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
): Innsending {

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    override fun somKomplettSøknad(søker: Søker, k9Format: Søknad?, titler: List<String>): KomplettInnsending = OmsorgsdagerKomplettMelding(
        søknadId = søknadId,
        søker = søker,
        mottatt = mottatt,
        id = id,
        språk = språk,
        barn = barn,
        mottakerFnr = mottakerFnr,
        mottakerNavn = mottakerNavn,
        harAleneomsorg = harAleneomsorg!!,
        harUtvidetRett = harUtvidetRett!!,
        erYrkesaktiv = erYrkesaktiv!!,
        arbeiderINorge = arbeiderINorge!!,
        arbeidssituasjon = arbeidssituasjon,
        antallDagerBruktIÅr = antallDagerBruktIÅr,
        type = type,
        korona = korona,
        overføring = overføring,
        fordeling = fordeling?.somKomplettFordele(),
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = harBekreftetOpplysninger
    )

    override fun valider() = mutableListOf<String>().apply {
        validerIdentifikator(mottakerFnr, "mottakerFnr")
        krever(barn.isNotEmpty(), "barn kan ikke være en tom liste.")
        krever(mottakerNavn.isNotBlank(), "mottakerNavn kan ikke være tomt eller blankt.")
        krever(arbeidssituasjon.isNotEmpty(), "arbeidssituasjon kan ikke være en tom liste.")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true.")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true.")

        validerType()
        validerKreverIkkeNull()
        addAll(barn.valider("barn"))

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    private fun MutableList<String>.validerKreverIkkeNull() {
        kreverIkkeNull(erYrkesaktiv, "erYrkesaktiv kan ikke være null. Må være true/false.")
        kreverIkkeNull(arbeiderINorge, "arbeiderINorge kan ikke være null. Må være true/false.")
        kreverIkkeNull(harAleneomsorg, "harAleneomsorg kan ikke være null. Må være true/false.")
        kreverIkkeNull(harUtvidetRett, "harUtvidetRett kan ikke være null. Må være true/false.")
    }

    private fun MutableList<String>.validerType() {
        when (type) {
            OVERFORING -> {
                kreverIkkeNull(overføring, "Ved type=OVERFORING kan ikke 'overføring' være null.")
                overføring?.valider("overføring")
            }
            FORDELING -> {
                kreverIkkeNull(fordeling, "Ved type=FORDELING kan ikke 'fordeling' være null.")
                fordeling?.valider("fordeling")
            }
            KORONA -> {
                kreverIkkeNull(korona, "Ved type=KORONA kan ikke 'korona' være null.")
                korona?.valider("korona")
            }
        }
    }

    override fun ytelse(): Ytelse = when(type) {
        FORDELING -> Ytelse.OMSORGSDAGER_MELDING_FORDELING
        OVERFORING -> Ytelse.OMSORGSDAGER_MELDING_OVERFORING
        KORONA -> Ytelse.OMSORGSDAGER_MELDING_KORONAOVERFORING
    }
    override fun søknadId(): String = søknadId
    override fun inneholderVedlegg() = (type == FORDELING && fordeling != null && fordeling.inneholderVedlegg())
    override fun vedlegg(): List<URL> = fordeling?.samværsavtale ?: listOf()
}
