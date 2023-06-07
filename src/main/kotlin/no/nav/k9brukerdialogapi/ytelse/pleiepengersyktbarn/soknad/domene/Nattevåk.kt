package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk.NattevåkPeriodeInfo
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.utils.StringUtils
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk as K9Nattevåk

data class Nattevåk(
    val harNattevåk: Boolean? = null,
    val tilleggsinformasjon: String?,
) {

    private companion object {
        private const val MAX_FRITEKST_TEGN = 1000
    }

    override fun toString(): String {
        return "Nattevåk(harNattevåk=${harNattevåk})"
    }

    fun tilK9Nattevåk(
        periode: no.nav.k9.søknad.felles.type.Periode,
    ): no.nav.k9.søknad.ytelse.psb.v1.Nattevåk? = K9Nattevåk().medPerioder(
        mapOf(
            periode to NattevåkPeriodeInfo().medTilleggsinformasjon(tilleggsinformasjon?.let { StringUtils.saniter(it) })
        )
    )

    fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(harNattevåk, "$felt.harNattevåk kan ikke være null")
        if (harNattevåk == true) {
            krever(
                tilleggsinformasjon !== null && tilleggsinformasjon.length <= MAX_FRITEKST_TEGN,
                "$felt.tilleggsinformasjon kan være max $MAX_FRITEKST_TEGN tegn, men var ${tilleggsinformasjon?.length}"
            )
        }

    }
}
