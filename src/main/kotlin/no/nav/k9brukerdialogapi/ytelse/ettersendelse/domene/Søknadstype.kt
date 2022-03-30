package no.nav.k9brukerdialogapi.ytelse.ettersendelse.domene

import no.nav.k9.ettersendelse.Ytelse

enum class Søknadstype {
    PLEIEPENGER_SYKT_BARN,
    PLEIEPENGER_LIVETS_SLUTTFASE,
    OMP_UTV_KS, // Omsorgspenger utvidet rett - kronisk syke eller funksjonshemming.
    OMP_UT_SNF, // Omsorgspenger utbetaling SNF ytelse.
    OMP_UT_ARBEIDSTAKER, // Omsorgspenger utbetaling arbeidstaker ytelse.
    OMP_UTV_MA, // Omsorgspenger utvidet rett - midlertidig alene
    OMP_DELE_DAGER;

    fun gjelderPleiepenger(): Boolean = this == PLEIEPENGER_SYKT_BARN || this == PLEIEPENGER_LIVETS_SLUTTFASE

    fun somK9Ytelse() = when(this){
        Søknadstype.OMP_UTV_KS -> Ytelse.OMP_UTV_KS
        Søknadstype.OMP_UTV_MA -> Ytelse.OMP_UTV_MA
        Søknadstype.PLEIEPENGER_SYKT_BARN -> Ytelse.PLEIEPENGER_SYKT_BARN
        Søknadstype.OMP_UT_SNF, Søknadstype.OMP_UT_ARBEIDSTAKER -> Ytelse.OMP_UT
        Søknadstype.OMP_DELE_DAGER -> Ytelse.OMP_DELE_DAGER
        Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE -> Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE
    }
}