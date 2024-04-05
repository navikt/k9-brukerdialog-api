package no.nav.k9brukerdialogapi.ytelse.ettersending.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.ettersendelse.Pleietrengende
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import java.time.LocalDate

data class Pleietrengende(
    var norskIdentitetsnummer: String?,
    val aktørId: String? = null,
    val navn: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate? = null,
) {
    override fun toString(): String {
        return "Pleietrengende(norskIdentitetsnummer=***, aktørId=***, navn=***, fodselsdato=***"
    }

    fun manglerIdentitetsnummer(): Boolean = norskIdentitetsnummer.isNullOrEmpty()

    infix fun oppdaterFødselsnummer(fødselsnummer: String?) {
        this.norskIdentitetsnummer = fødselsnummer
    }

    fun tilK9Pleietrengende(): Pleietrengende = when {
        norskIdentitetsnummer != null -> Pleietrengende(NorskIdentitetsnummer.of(norskIdentitetsnummer))
        else -> error("Mangler identitetsnummer")
    }
}

internal fun no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Pleietrengende.valider(felt: String) =
    mutableListOf<String>().apply {
        validerIdentifikator(norskIdentitetsnummer, "$felt.fødselsnummer")
    }
