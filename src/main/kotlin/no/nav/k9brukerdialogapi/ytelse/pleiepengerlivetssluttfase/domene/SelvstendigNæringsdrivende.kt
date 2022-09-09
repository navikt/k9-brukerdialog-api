package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

class SelvstendigNæringsdrivende(
    val virksomhet: no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende,
    val arbeidsforhold: Arbeidsforhold
) {
    internal fun valider(felt: String = "selvstendigNæringsdrivende") = mutableListOf<String>().apply{
        addAll(virksomhet.valider("$felt.virksomhet"))
        addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
    }
}