package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import java.time.Duration
import kotlin.test.Test

class ArbeidIPeriodeTest {

    companion object {
        private val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_PROSENT_AV_NORMALT og prosentAvNormalt er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            prosentAvNormalt = null
        )
            .valider("test")
            .verifiserFeil(1, listOf("test.prosentAvNormalt må være satt dersom type=ARBEIDER_PROSENT_AV_NORMALT"))
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_TIMER_I_SNITT_PER_UKE og timerPerUke er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            timerPerUke = null
        ).valider("test").verifiserFeil(1, listOf("test.timerPerUke må være satt dersom type=ARBEIDER_TIMER_I_SNITT_PER_UKE"))
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_ULIKE_UKER_TIMER og arbeidsuker er null eller tom`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_ULIKE_UKER_TIMER,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            arbeidsuker = null
        ).valider("test").verifiserFeil(1, listOf("test.arbeidsuker må være satt dersom type=ARBEIDER_ULIKE_UKER_TIMER"))
    }
}
