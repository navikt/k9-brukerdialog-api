package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende

data class Regnskapsfører(
    private val navn: String,
    private val telefon: String
) {
    companion object{
        internal fun SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.leggTilK9Regnskapsfører(regnskapsfører: Regnskapsfører) {
            medRegnskapsførerNavn(regnskapsfører.navn)
            medRegnskapsførerTlf(regnskapsfører.telefon)
        }
    }
}
