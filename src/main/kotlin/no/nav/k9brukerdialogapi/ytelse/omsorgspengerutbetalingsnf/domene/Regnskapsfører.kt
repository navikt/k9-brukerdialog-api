package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende

class Regnskapsfører(
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