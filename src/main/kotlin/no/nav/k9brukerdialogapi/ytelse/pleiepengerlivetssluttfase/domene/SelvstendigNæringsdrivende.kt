package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class SelvstendigNæringsdrivende(
    val virksomhet: Virksomhet,
    val arbeidsforhold: Arbeidsforhold
) {
    internal fun valider(felt: String = "selvstendigNæringsdrivende") = mutableListOf<String>().apply{
        addAll(virksomhet.valider("$felt.virksomhet"))
        addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
    }

    fun somK9SelvstendigNæringsdrivende() = virksomhet.somK9SelvstendigNæringsdrivende()
    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) = arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)
}
