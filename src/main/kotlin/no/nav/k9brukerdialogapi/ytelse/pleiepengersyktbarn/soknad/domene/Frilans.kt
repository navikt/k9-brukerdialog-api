package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.LocalDate

data class Frilans(
    val harInntektSomFrilanser: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate? = null,
    val arbeidsforhold: Arbeidsforhold? = null,
    val frilansTyper: List<FrilansType>? = null,
    val misterHonorarer: Boolean? = null,
    val misterHonorarerIPerioden: MisterHonorarerFraVervIPerioden? = null
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        if(arbeidsforhold != null) addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
        if(harInntektSomFrilanser){
            kreverIkkeNull(startdato, "$felt.startdato kan ikke være null dersom harInntektSomFrilanser=true")
        }
        if (misterHonorarer != null && misterHonorarer) {
            kreverIkkeNull(misterHonorarerIPerioden, "$felt.misterHonorarerIPerioden kan ikke være null dersom misterHonorarer=true")
        }
    }

    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return when{
            (arbeidsforhold == null) -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
            startetISøknadsperioden(fraOgMed) -> k9ArbeidstidInfoMedStartIPerioden(fraOgMed, tilOgMed)
            else -> arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
        }
    }

    private fun k9ArbeidstidInfoMedStartIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(startdato)
        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, startdato.minusDays(1))
        val arbeidsforholdEtterStart = arbeidsforhold.tilK9ArbeidstidInfo(startdato, tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdEtterStart)
    }

    private fun slåSammenArbeidstidInfo(vararg arbeidstidInfo: ArbeidstidInfo): ArbeidstidInfo {
        return ArbeidstidInfo().apply {
            arbeidstidInfo.forEach { arbeidstidInfo: ArbeidstidInfo ->
                arbeidstidInfo.perioder.forEach { (periode, arbeidstidPeriodeInfo): Map.Entry<no.nav.k9.søknad.felles.type.Periode, ArbeidstidPeriodeInfo> ->
                    this.leggeTilPeriode(
                        periode,
                        arbeidstidPeriodeInfo
                    )
                }
            }
        }
    }

    private fun startetISøknadsperioden(fraOgMed: LocalDate) = startdato?.isAfter(fraOgMed) ?: false
}

enum class MisterHonorarerFraVervIPerioden {
    MISTER_ALLE_HONORARER, MISTER_DELER_AV_HONORARER
}

enum class FrilansType {
    FRILANS,
    STYREVERV
}
