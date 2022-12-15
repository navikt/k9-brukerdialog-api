package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.pls.v1.Pleietrengende as K9Pleietrengende

class Pleietrengende(
    private val norskIdentitetsnummer: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") private val fødselsdato: LocalDate? = null,
    private val navn: String,
    private val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
) {
    internal fun somK9Pleietrengende(): K9Pleietrengende = when {
        norskIdentitetsnummer != null -> K9Pleietrengende().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(norskIdentitetsnummer))
        fødselsdato != null -> K9Pleietrengende().medFødselsdato(fødselsdato)
        else -> K9Pleietrengende()
    }

    fun valider(felt: String = "pleietrengende") = mutableListOf<String>().apply {
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.")
        fødselsdato?.let { krever(it.erFørEllerLik(LocalDate.now()), "$felt.fødselsdato kan ikke være i fremtiden.") }
        if(norskIdentitetsnummer == null){
            krever(fødselsdato != null, "$felt.fødselsdato må være satt dersom norskIdentitetsnummer er null.")
            krever(årsakManglerIdentitetsnummer != null, "$felt.årsakManglerIdentitetsnummer må være satt dersom norskIdentitetsnummer er null.")
        } else {
            validerIdentifikator(norskIdentitetsnummer, "$felt.norskIdentitetsnummer")
        }
    }
}

enum class ÅrsakManglerIdentitetsnummer { BOR_I_UTLANDET, ANNET }
