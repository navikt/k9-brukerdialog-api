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
        arbeidsforhold?.let { addAll(it.valider("$felt.arbeidsforhold")) }
        virksomhet?.let { addAll(it.valider("$felt.virksomhet")) }
    }

    fun tilK9SelvstendigNæringsdrivende(): SelvstendigNæringsdrivende {
        requireNotNull(virksomhet)
        return virksomhet.somK9SelvstendigNæringsdrivende()
    }

    fun k9ArbeidstidInfo(søknadsperiodeStartdato: LocalDate, søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        return when{
            (arbeidsforhold == null) -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(søknadsperiodeStartdato, søknadsperiodeSluttdato)
            startetOgSluttetISøknadsperioden(søknadsperiodeStartdato, søknadsperiodeSluttdato) -> k9ArbeidstidInfoMedStartOgSluttIPerioden(søknadsperiodeSluttdato)
            sluttetISøknadsperioden(søknadsperiodeSluttdato) -> k9ArbeidstidInfoMedSluttIPerioden(søknadsperiodeStartdato, søknadsperiodeSluttdato)
            startetISøknadsperioden(søknadsperiodeStartdato) -> k9ArbeidstidInfoMedStartIPerioden(søknadsperiodeSluttdato)
            else -> arbeidsforhold.tilK9ArbeidstidInfo(søknadsperiodeStartdato, søknadsperiodeSluttdato)
        }
    }

    private fun sluttetISøknadsperioden(søknadsperiodeSluttdato: LocalDate?): Boolean {
        requireNotNull(virksomhet)
        val virksomhetSluttdato = virksomhet.tilOgMed

        return virksomhetSluttdato != null && virksomhetSluttdato.isBefore(søknadsperiodeSluttdato)
    }
    private fun k9ArbeidstidInfoMedSluttIPerioden(søknadperiodeStartdato: LocalDate, søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        return arbeidsforhold.tilK9ArbeidstidInfo(søknadperiodeStartdato, søknadsperiodeSluttdato)
    }

    private fun startetISøknadsperioden(søknadsperiodeStartdato: LocalDate): Boolean {
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed

        return virksomhetStartdato.isAfter(søknadsperiodeStartdato)
    }
    private fun k9ArbeidstidInfoMedStartIPerioden(søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed

        return arbeidsforhold.tilK9ArbeidstidInfo(virksomhetStartdato, søknadsperiodeSluttdato)
    }

    private fun startetOgSluttetISøknadsperioden(fraOgMed: LocalDate, tilOgMed: LocalDate?) = sluttetISøknadsperioden(tilOgMed) && startetISøknadsperioden(fraOgMed)
    private fun k9ArbeidstidInfoMedStartOgSluttIPerioden(søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed
        requireNotNull(virksomhetStartdato)

        return arbeidsforhold.tilK9ArbeidstidInfo(virksomhetStartdato, søknadsperiodeSluttdato)
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
