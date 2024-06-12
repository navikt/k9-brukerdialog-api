package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn
import java.time.LocalDate

class Barn(
    private var identitetsnummer: String? = null,
    private val aktørId: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") private val fødselsdato: LocalDate,
    private val navn: String,
    private val type: TypeBarn
) {
    companion object {
        internal fun List<Barn>.somK9BarnListe() = kunFosterbarn().map { it.somK9Barn() }
        private fun List<Barn>.kunFosterbarn() = this.filter { it.type == TypeBarn.FOSTERBARN }
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

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(identitetsnummer == null) identitetsnummer = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun somK9Barn(): K9Barn {
        val barn = K9Barn()
        if (identitetsnummer != null) {
            barn.medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer));
        } else {
            barn.medFødselsdato(fødselsdato)
        }
        return barn
    }

}

enum class TypeBarn {
    FRA_OPPSLAG,
    FOSTERBARN,
    ANNET
}

