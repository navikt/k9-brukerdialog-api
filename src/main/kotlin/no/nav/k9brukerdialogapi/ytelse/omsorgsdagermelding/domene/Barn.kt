package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
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

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(identitetsnummer, "$felt.identitetsnummer")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blank.")
        kreverIkkeNull(aleneOmOmsorgen, "$felt.aleneOmOmsorgen kan ikke være null. Må være true/false.")
        kreverIkkeNull(utvidetRett, "$felt.utvidetRett kan ikke være null. Må være true/false.")
    }

    internal fun manglerIdentitetsnummer() = identitetsnummer.isNullOrBlank()

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(manglerIdentitetsnummer()) identitetsnummer = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

}