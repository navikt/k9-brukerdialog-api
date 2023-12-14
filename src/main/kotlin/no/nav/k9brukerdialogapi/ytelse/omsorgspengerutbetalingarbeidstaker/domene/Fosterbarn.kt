package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Fosterbarn(
    private val navn: String,
    private val fødselsdato: LocalDate,
    internal val utvidetRett: Boolean? = null,
    private var identitetsnummer: String? = null
) {
    companion object{
        internal fun List<Fosterbarn>.somK9BarnListe() = map { it.somK9Barn() }
        internal fun List<Fosterbarn>.valider(felt: String) = this.flatMapIndexed { index, barn ->
            barn.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(identitetsnummer, "$felt.identitetsnummer")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.")
    }

    internal fun trettenÅrEllerEldre() = LocalDate.now().year.minus(fødselsdato.year) >= 13
    internal fun somK9Barn() = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))
}
