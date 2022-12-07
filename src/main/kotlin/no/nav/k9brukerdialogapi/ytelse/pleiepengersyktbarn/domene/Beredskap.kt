package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.k9.søknad.ytelse.psb.v1.Beredskap.BeredskapPeriodeInfo
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9.søknad.felles.type.Periode as K9Periode
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap as K9Beredskap

data class Beredskap(
    val beredskap: Boolean,
    val tilleggsinformasjon: String?,
) {
    private companion object {
        private const val MAX_FRITEKST_TEGN = 1000
    }
    override fun toString(): String {
        return "Beredskap(beredskap=${beredskap})"
    }

    fun tilK9Beredskap(periode: K9Periode) = K9Beredskap()
        .medPerioder(
            mapOf(
                periode to BeredskapPeriodeInfo().medTilleggsinformasjon(
                    tilleggsinformasjon
                )
            )
        )

    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(tilleggsinformasjon !== null && tilleggsinformasjon.length < MAX_FRITEKST_TEGN,
            "$felt.tilleggsinformasjon kan være max $MAX_FRITEKST_TEGN tegn, men var ${tilleggsinformasjon?.length}")
    }
}
