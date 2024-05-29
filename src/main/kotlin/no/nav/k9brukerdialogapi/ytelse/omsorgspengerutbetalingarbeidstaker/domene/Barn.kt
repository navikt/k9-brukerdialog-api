package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    private var identitetsnummer: String? = null,
    private val aktørId: String,
    @JsonFormat(pattern = "yyyy-MM-dd") private val fødselsdato: LocalDate,
    private val navn: String,
    private val type: TypeBarn
) {
    companion object {
        internal fun List<Barn>.somK9BarnListe() = map { it.somK9Barn() }
        internal fun List<Barn>.valider(felt: String) = this.flatMapIndexed { index, barn ->
            barn.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(identitetsnummer, "$felt.identitetsnummer")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.")
        krever(
            fødselsdato.erLikEllerEtter(LocalDate.now().minusYears(19)),
            "$felt.fødselsdato kan ikke være mer enn 19 år siden."
        )
        krever(
            fødselsdato.erFørEllerLik(LocalDate.now()),
            "$felt.fødselsdato kan ikke være i fremtiden."
        )
    }

    internal fun somK9Barn() =
        K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer)).medFødselsdato(fødselsdato)

}

enum class TypeBarn {
    FRA_OPPSLAG,
    FOSTERBARN,
    ANNET
}

