package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Land
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Næringstype
import java.time.LocalDate

class UtenlandskNæring(
    val næringstype: Næringstype,
    val navnPåVirksomheten: String,
    val land: Land,
    val organisasjonsnummer: String? = null,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null
) {

    companion object {
        internal fun List<UtenlandskNæring>.valider(felt: String) = flatMapIndexed { index, utenlandskNæring ->
            utenlandskNæring.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        tilOgMed?.let { krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed.") }
        addAll(land.valider("$felt.land"))
    }

}