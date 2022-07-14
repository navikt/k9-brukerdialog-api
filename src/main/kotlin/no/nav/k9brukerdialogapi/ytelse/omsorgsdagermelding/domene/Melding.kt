package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Barn.Companion.valider
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
    private val arbeidssituasjon: List<Arbeidssituasjon>,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {
    fun valider() = mutableListOf<String>().apply {
        validerIdentifikator(mottakerFnr, "mottakerFnr")
        krever(barn.isNotEmpty(), "barn kan ikke være en tom liste.")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true.")
        krever(mottakerNavn.isNotBlank(), "mottakerNavn kan ikke være tomt eller blankt.")
        krever(arbeidssituasjon.isNotEmpty(), "arbeidssituasjon kan ikke være en tom liste.")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true.")
        kreverIkkeNull(harAleneomsorg, "harAleneomsorg kan ikke være null. Må være true/false.")
        kreverIkkeNull(harUtvidetRett, "harUtvidetRett kan ikke være null. Må være true/false.")

        addAll(barn.valider("barn"))
        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }
}