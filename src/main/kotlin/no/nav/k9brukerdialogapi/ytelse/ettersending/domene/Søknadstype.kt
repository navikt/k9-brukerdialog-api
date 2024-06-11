package no.nav.k9brukerdialogapi.ytelse.ettersending.domene

import no.nav.k9.ettersendelse.Ytelse

enum class Søknadstype {
    PLEIEPENGER_SYKT_BARN,
    PLEIEPENGER_LIVETS_SLUTTFASE,
    OMP_UT_SNF, // Omsorgspenger utbetaling SNF ytelse.
    OMP_UT_ARBEIDSTAKER, // Omsorgspenger utbetaling arbeidstaker ytelse.
    OMP_UTV_KS, // Omsorgspenger utvidet rett - kronisk syke eller funksjonshemming.
    OMP_UTV_MA, // Omsorgspenger utvidet rett - midlertidig alene
    OMP_UTV_AO;

    fun somK9Ytelse() = when (this) {
        OMP_UTV_KS -> Ytelse.OMP_UTV_KS
        OMP_UTV_MA -> Ytelse.OMP_UTV_MA
        PLEIEPENGER_SYKT_BARN -> Ytelse.PLEIEPENGER_SYKT_BARN
        OMP_UT_SNF, OMP_UT_ARBEIDSTAKER -> Ytelse.OMP_UT
        OMP_UTV_AO -> Ytelse.OMP_UTV_AO
        PLEIEPENGER_LIVETS_SLUTTFASE -> Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE
    }
}
