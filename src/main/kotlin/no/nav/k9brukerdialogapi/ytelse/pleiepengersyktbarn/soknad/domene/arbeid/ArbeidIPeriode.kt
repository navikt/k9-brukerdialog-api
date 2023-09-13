package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid

import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Periode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType.PROSENT_AV_NORMALT
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType.ULIKE_UKER_TIMER
import java.time.Duration

data class ArbeidsRedusert(
    val type: RedusertArbeidstidType,
    val prosentAvNormalt: Double? = null,
    val timerPerUke: Duration? = null,
    val arbeidsuker: List<ArbeidsUke>? = null
) {
    fun valider(felt: String) = mutableListOf<String>().apply {
        when(type){
            PROSENT_AV_NORMALT -> kreverIkkeNull(prosentAvNormalt, "$felt.prosentAvNormalt må være satt dersom type=PROSENT_AV_NORMALT")
            TIMER_I_SNITT_PER_UKE -> kreverIkkeNull(timerPerUke, "$felt.timerPerUke må være satt dersom type=TIMER_I_SNITT_PER_UKE")
            ULIKE_UKER_TIMER -> kreverIkkeNull(arbeidsuker, "$felt.arbeidsuker må være satt dersom type=ULIKE_UKER_TIMER")
        }
    }
}

data class ArbeidIPeriode(
    val type: ArbeidIPeriodeType,
    val redusertArbeid: ArbeidsRedusert? = null,
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        when(type){
            ArbeidIPeriodeType.ARBEIDER_REDUSERT -> {
                kreverIkkeNull(redusertArbeid, "$felt.redusertArbeid må være satt dersom type=ARBEIDER_REDUSERT")
                redusertArbeid?.valider("$felt.redusertArbeid")?.forEach { add(it) }
            }
            ArbeidIPeriodeType.ARBEIDER_VANLIG, ArbeidIPeriodeType.ARBEIDER_IKKE -> {} // Ikke noe å validere
        }
    }

    override fun equals(other: Any?) = this === other || other is ArbeidIPeriode && this.equals(other)
    private fun equals(other: ArbeidIPeriode) = this.type == other.type && this.redusertArbeid == other.redusertArbeid

}

data class ArbeidsUke(val periode: Periode, val timer: Duration)
