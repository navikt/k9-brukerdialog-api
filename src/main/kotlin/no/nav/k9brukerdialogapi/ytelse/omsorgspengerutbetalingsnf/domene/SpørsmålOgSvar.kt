package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull

class SpørsmålOgSvar(
    private val spørsmål: String,
    private val svar: Boolean?
) {
    companion object{
        private const val MAX_FRITEKST_TEGN = 1000

        internal fun List<SpørsmålOgSvar>.valider(felt: String) = this.flatMapIndexed { index, spørsmålOgSvar ->
            spørsmålOgSvar.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(svar, "$felt.svar kan ikke være null. Må være true/false.")
        krever(spørsmål.isNotBlank(), "$felt.spørsmål kan ikke være tomt eller blankt.")
        krever(spørsmål.length <= MAX_FRITEKST_TEGN, "$felt.spørsmål kan ikke være mer enn $MAX_FRITEKST_TEGN tegn.")
    }
}