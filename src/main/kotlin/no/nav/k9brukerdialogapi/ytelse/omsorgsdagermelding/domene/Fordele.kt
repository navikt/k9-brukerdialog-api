package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.SAMVÆRSFORELDER
import java.net.URL

class Fordele(
    private val mottakerType: Mottaker,
    internal val samværsavtale: List<URL> = listOf()
){
    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(mottakerType == SAMVÆRSFORELDER, "$felt.mottakerType må være '$SAMVÆRSFORELDER'.")
    }

    internal fun inneholderVedlegg() = samværsavtale.isNotEmpty()

    internal fun somKomplettFordele() = KomplettFordele(
        mottakerType = mottakerType,
        samværsavtaleVedleggId = samværsavtale.map { url -> url.vedleggId() }
    )
}

class KomplettFordele(
    private val mottakerType: Mottaker,
    internal val samværsavtaleVedleggId: List<String>
)