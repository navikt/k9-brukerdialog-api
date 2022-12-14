package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import java.time.LocalDate

data class OpptjeningIUtlandet(
    val navn: String,
    val opptjeningType: OpptjeningType,
    val land: Land,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
)

enum class OpptjeningType {
    ARBEIDSTAKER,
    FRILANSER
}

internal fun List<OpptjeningIUtlandet>.valider() = mutableListOf<String>().apply {
    this@valider.forEachIndexed { index, opptjeningIUtlandet -> addAll(opptjeningIUtlandet.valider("opptjeningIUtlandet[$index]")) }
}

private fun OpptjeningIUtlandet.valider(felt: String) = mutableListOf<String>().apply {
    this.addAll(land.valider("$felt.land"))
}
