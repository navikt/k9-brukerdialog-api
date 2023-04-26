package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.LocalDate

data class StønadGodtgjørelse(
    private val mottarStønadGodtgjørelse: Boolean?,
    private val startdato: LocalDate? = null,
    private val sluttdato: LocalDate? = null
) {

    fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(mottarStønadGodtgjørelse, "$felt.mottarStønadGodtgjørelse kan ikke være null")
        TODO("Skriv flere valideringer")
    }
}
