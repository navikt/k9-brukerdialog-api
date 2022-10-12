package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Ferieuttak(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate
) {
    override fun toString(): String {
        return "Ferieuttak(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
    }
}