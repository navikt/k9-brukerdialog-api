package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid.Arbeidsforhold
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
    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo? {
       return arbeidsforhold?.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
    }
}
