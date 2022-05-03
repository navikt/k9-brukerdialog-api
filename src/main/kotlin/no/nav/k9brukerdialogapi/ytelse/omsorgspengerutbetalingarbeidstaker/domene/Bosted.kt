package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

typealias Opphold = Bosted

class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,
    private val landkode: String,
    private val landnavn: String,
    private val erEØSLand: Boolean? = null
){
    init {
        requireNotNull(erEØSLand) { "erEØSLand må være satt." }
        require(!fraOgMed.isAfter(tilOgMed)) { "fraOgMed kan ikke være etter tilOgMed," }
        require(landnavn.isNotBlank()) { "landnavn kan ikke være blankt eller tromt." }
        require(landkode.isNotBlank()) { "landkode kan ikke være blankt eller tromt." }
    }
}
