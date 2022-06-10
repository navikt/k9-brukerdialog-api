package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn.FRA_OPPSLAG
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    private val navn: String,
    private val fødselsdato: LocalDate,
    private val type: TypeBarn,
    private val aktørId: String? = null,
    internal val utvidetRett: Boolean? = null,
    private var identitetsnummer: String? = null
) {
    companion object{
        internal fun List<Barn>.somK9BarnListe() = this.filter { it.type == FRA_OPPSLAG }.map { it.somK9Barn() }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(identitetsnummer, "$felt.identitetsnummer")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.")
    }

    internal fun trettenÅrEllerEldre() = LocalDate.now().year.minus(fødselsdato.year) >= 12
    internal fun somK9Barn() = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))
}

enum class TypeBarn{
    FOSTERBARN,
    ANNET,
    FRA_OPPSLAG
}