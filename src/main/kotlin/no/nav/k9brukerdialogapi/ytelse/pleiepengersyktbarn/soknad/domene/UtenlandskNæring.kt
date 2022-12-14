package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
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
        internal fun List<UtenlandskNæring>.valider(felt: String) = this.flatMapIndexed { index, utenlandskNæring ->
            utenlandskNæring.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(land.valider("$felt.land"))
        tilOgMed?.let { krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed") }
    }

    override fun equals(other: Any?) = other === this || other is UtenlandskNæring && this.equals(other)
    private fun equals(other: UtenlandskNæring) = this.organisasjonsnummer == other.organisasjonsnummer && this.navnPåVirksomheten == other.navnPåVirksomheten
}

internal fun LocalDate.erLikEllerEtter(tilOgMedDato: LocalDate) = this.isEqual(tilOgMedDato) || this.isAfter(tilOgMedDato)
