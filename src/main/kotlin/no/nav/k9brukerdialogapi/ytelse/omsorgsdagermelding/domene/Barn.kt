package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import java.time.LocalDate

class Barn(
    internal val aktørId: String? = null,
    private var identitetsnummer: String? = null,
    private val fødselsdato: LocalDate,
    private val navn: String,
    private val aleneOmOmsorgen: Boolean? = null,
    private val utvidetRett: Boolean? = null
) {
    companion object {
        internal fun List<Barn>.valider(felt: String) = flatMapIndexed { index: Int, barn: Barn ->
            barn.valider("$felt[$index]")
        }
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(identitetsnummer, "$felt.identitetsnummer")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blank.")
        kreverIkkeNull(aleneOmOmsorgen, "$felt.aleneOmOmsorgen kan ikke være null. Må være true/false.")
        kreverIkkeNull(utvidetRett, "$felt.utvidetRett kan ikke være null. Må være true/false.")
    }

    fun manglerIdentitetsnummer() = identitetsnummer.isNullOrBlank()
    fun oppdaterIdentitetsnummerMed(identitetsnummer: String) {
        require(manglerIdentitetsnummer()) { "Kan kun oppdatere identitetsnummer på barn som mangler." }
        this.identitetsnummer = identitetsnummer
    }

}