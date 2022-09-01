package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Land
import java.time.LocalDate

class OpptjeningIUtlandet (
    val navn: String,
    val opptjeningType: OpptjeningType,
    val land: Land,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
) {
    companion object {
        internal fun List<OpptjeningIUtlandet>.valider(felt: String) = flatMapIndexed { index, opptjeningIUtlandet ->
            opptjeningIUtlandet.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed.")
        addAll(land.valider("$felt.land"))
    }
}

enum class OpptjeningType { ARBEIDSTAKER, FRILANSER }