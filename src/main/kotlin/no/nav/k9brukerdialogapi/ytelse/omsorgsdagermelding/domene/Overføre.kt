package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.EKTEFELLE
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.SAMBOER

class Overføre(
    private val mottakerType: Mottaker,
    private val antallDagerSomSkalOverføres: Int
) {
    companion object{
        internal val gyldigeMottakere = listOf(SAMBOER, EKTEFELLE)
        internal val gyldigDagerRange = (1..10)
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(mottakerType in gyldigeMottakere, "$felt.mottakerType må være en av $gyldigeMottakere.")
        krever(antallDagerSomSkalOverføres in gyldigDagerRange, "$felt.antallDagerSomSkalOverføres må være innenfor range $gyldigDagerRange.")
    }

}