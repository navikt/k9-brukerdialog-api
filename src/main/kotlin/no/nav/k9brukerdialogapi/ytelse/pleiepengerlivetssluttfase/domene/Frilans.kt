package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: Boolean,
    val arbeidsforhold: Arbeidsforhold? = null,
    val harHattInntektSomFrilanser: Boolean? = null
) {
    internal fun valider(felt: String = "frilans") = mutableListOf<String>().apply {
        kreverIkkeNull(harHattInntektSomFrilanser, "$felt.harHattInntektSomFrilanser kan ikke være null.")
        if(sluttdato != null) krever(sluttdato.erLikEllerEtter(startdato), "$felt.sluttdato må være lik eller etter startdato.")
        if(jobberFortsattSomFrilans) krever(sluttdato == null, "$felt.sluttdato kan ikke være satt dersom jobberFortsattSomFrilans er true.")
        if(!jobberFortsattSomFrilans) krever(sluttdato != null, "$felt.sluttdato må være satt dersom jobberFortsattSomFrilans er false.")
        arbeidsforhold?.let { addAll(it.valider("$felt.arbeidsforhold")) }
    }

    internal fun somK9Frilanser() = Frilanser().apply {
        medStartdato(this@Frilans.startdato)
        this@Frilans.sluttdato?.let { medSluttdato(this@Frilans.sluttdato) }
    }

    internal fun somK9Arbeidstid(fraOgMed: LocalDate, tilOgMed: LocalDate) = arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)

}
