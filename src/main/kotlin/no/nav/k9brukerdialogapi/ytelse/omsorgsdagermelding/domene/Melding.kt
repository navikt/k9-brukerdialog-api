package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Barn.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Meldingstype.*
import java.util.*

class Melding(
    private val søknadId: String = UUID.randomUUID().toString(),
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
    private val type: Meldingstype,
    private val korona: Koronaoverføre? = null,
    private val overføring: Overføre? = null,
    private val fordeling: Fordele? = null,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {
    fun valider() = mutableListOf<String>().apply {
        validerIdentifikator(mottakerFnr, "mottakerFnr")
        krever(barn.isNotEmpty(), "barn kan ikke være en tom liste.")
        krever(mottakerNavn.isNotBlank(), "mottakerNavn kan ikke være tomt eller blankt.")
        krever(arbeidssituasjon.isNotEmpty(), "arbeidssituasjon kan ikke være en tom liste.")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true.")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true.")

        addAll(barn.valider("barn"))
        validerKreverIkkeNull()
        validerType()

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
}