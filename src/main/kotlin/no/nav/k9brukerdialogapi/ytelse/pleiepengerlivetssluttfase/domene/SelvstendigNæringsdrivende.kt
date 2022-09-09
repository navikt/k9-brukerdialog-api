package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class SelvstendigNæringsdrivende(
    val virksomhet: no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende,
    val arbeidsforhold: Arbeidsforhold
) {
    internal fun valider(felt: String = "selvstendigNæringsdrivende") = mutableListOf<String>().apply{
        addAll(virksomhet.valider("$felt.virksomhet"))
        addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
    }

    fun somK9SelvstendigNæringsdrivende() = virksomhet.somK9SelvstendigNæringsdrivende()
    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) = arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)
}