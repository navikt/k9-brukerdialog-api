package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import java.time.Duration
import java.time.LocalDate

class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration
){
    companion object{
        internal fun List<Enkeltdag>.finnTidForGittDato(dato: LocalDate) = this.find { it.dato == dato }?.tid
    }
}
