package no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Barn(
    var norskIdentifikator: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fødselsdato: LocalDate? = null,
    val aktørId: String? = null,
    val navn: String
) {
    fun manglerNorskIdentifikator(): Boolean = norskIdentifikator.isNullOrEmpty()

    infix fun oppdaterNorskIdentifikatorMed(norskIdentifikator: String?){
        this.norskIdentifikator = norskIdentifikator
    }

    override fun toString(): String {
        return "Barn(aktoerId=${aktørId}, navn=${navn}, fodselsdato=${fødselsdato}"
    }
}