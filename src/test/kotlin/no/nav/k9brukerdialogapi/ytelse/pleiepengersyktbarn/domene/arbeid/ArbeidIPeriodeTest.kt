package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidsRedusert
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import java.time.Duration
import kotlin.test.Test

class ArbeidIPeriodeTest {

    companion object {
        private val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
    }

    @Test
    fun `Skal gi feil dersom type=PROSENT_AV_NORMALT og prosentAvNormalt er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.REDUSERT,
            redusertArbeid = ArbeidsRedusert(
                type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                prosentAvNormalt = null
            )
        )
            .valider("test")
            .verifiserFeil(1, listOf("test.redusertArbeid.prosentAvNormalt må være satt dersom type=PROSENT_AV_NORMALT"))
    }

    @Test
    fun `Skal gi feil dersom type=TIMER_I_SNITT_PER_UKE og timerPerUke er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.REDUSERT,
            redusertArbeid = ArbeidsRedusert(
                type = RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE,
                timerPerUke = null
            )
        ).valider("test")
            .verifiserFeil(1, listOf("test.redusertArbeid.timerPerUke må være satt dersom type=TIMER_I_SNITT_PER_UKE"))
    }

    @Test
    fun `Skal gi feil dersom type=ULIKE_UKER_TIMER og arbeidsuker er null eller tom`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.REDUSERT,
            redusertArbeid = ArbeidsRedusert(
                type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                arbeidsuker = null
            )
        ).valider("test")
            .verifiserFeil(1, listOf("test.redusertArbeid.arbeidsuker må være satt dersom type=ULIKE_UKER_TIMER"))
    }
}
