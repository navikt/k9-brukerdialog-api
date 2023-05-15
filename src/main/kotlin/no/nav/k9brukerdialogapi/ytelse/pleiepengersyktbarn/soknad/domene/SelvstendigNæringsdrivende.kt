package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import java.time.LocalDate

data class SelvstendigNæringsdrivende(
    val harInntektSomSelvstendig: Boolean,
    val virksomhet: Virksomhet? = null,
    val arbeidsforhold: Arbeidsforhold? = null,
) {
    internal fun valider(felt: String = "selvstendigNæringsdrivende") = mutableListOf<String>().apply {
        if (harInntektSomSelvstendig) {
            kreverIkkeNull(arbeidsforhold, "$felt.arbeidsforhold må være satt når man har harInntektSomSelvstendig.")
            kreverIkkeNull(virksomhet, "$felt.virksomhet må være satt når man har harInntektSomSelvstendig.")
        }
        arbeidsforhold?.let { addAll(it.valider("$felt.arbeidsforhold", true)) }
        virksomhet?.let { addAll(it.valider("$felt.virksomhet")) }
    }

    fun tilK9SelvstendigNæringsdrivende(): SelvstendigNæringsdrivende {
        requireNotNull(virksomhet)
        return virksomhet.somK9SelvstendigNæringsdrivende()
    }

    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return when{
            (arbeidsforhold == null) -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
            startetOgSluttetISøknadsperioden(fraOgMed, tilOgMed) -> k9ArbeidstidInfoMedStartOgSluttIPerioden(fraOgMed, tilOgMed)
            sluttetISøknadsperioden(tilOgMed) -> k9ArbeidstidInfoMedSluttIPerioden(fraOgMed, tilOgMed)
            startetISøknadsperioden(fraOgMed) -> k9ArbeidstidInfoMedStartIPerioden(fraOgMed, tilOgMed)
            else -> arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
        }
    }

    private fun sluttetISøknadsperioden(tilOgMed: LocalDate?): Boolean {
        requireNotNull(virksomhet)
        val virksomhetSluttdato = virksomhet.tilOgMed

        return virksomhetSluttdato != null && virksomhetSluttdato.isBefore(tilOgMed)
    }
    private fun k9ArbeidstidInfoMedSluttIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        requireNotNull(virksomhet.tilOgMed)
        val arbeidsforholdFørSlutt = arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, virksomhet.tilOgMed)
        val arbeidsforholdEtterSlutt = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(virksomhet.tilOgMed.plusDays(1), tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørSlutt, arbeidsforholdEtterSlutt)
    }

    private fun startetISøknadsperioden(fraOgMed: LocalDate): Boolean {
        requireNotNull(virksomhet)
        val virksomhetStartdatp = virksomhet.fraOgMed

        return virksomhetStartdatp.isAfter(fraOgMed)
    }
    private fun k9ArbeidstidInfoMedStartIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed

        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, virksomhetStartdato.minusDays(1))
        val arbeidsforholdEtterStart = arbeidsforhold.tilK9ArbeidstidInfo(virksomhetStartdato, tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdEtterStart)
    }

    private fun startetOgSluttetISøknadsperioden(fraOgMed: LocalDate, tilOgMed: LocalDate?) = sluttetISøknadsperioden(tilOgMed) && startetISøknadsperioden(fraOgMed)
    private fun k9ArbeidstidInfoMedStartOgSluttIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        val virksomhetSluttdato = virksomhet.tilOgMed
        requireNotNull(virksomhetSluttdato)
        val virksomhetStartdato = virksomhet.fraOgMed

        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, virksomhetStartdato.minusDays(1))
        val arbeidsforholdMedArbeid = arbeidsforhold.tilK9ArbeidstidInfo(virksomhetStartdato, virksomhetSluttdato)
        val arbeidsforholdEtterSlutt = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(virksomhetSluttdato.plusDays(1), tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdMedArbeid, arbeidsforholdEtterSlutt)
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
}
