package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold.Companion.k9ArbeidstidInfoMedNullTimer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9brukerdialogapi.general.krever
import java.time.LocalDate

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null
) {
    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return arbeidsforhold?.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
            ?: k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        if(arbeidsforhold != null) addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
        krever(organisasjonsnummer.erGyldigOrganisasjonsnummer(), "$felt.organisasjonsnummer må være gyldig")
        krever(!navn.isNullOrBlank(), "$felt.navn kan ikke være tomt eller kun whitespace")
    }
}

internal fun List<Arbeidsgiver>.valider() = mutableSetOf<String>().apply {
    this@valider.mapIndexed { index, arbeidsgiver ->
        addAll(arbeidsgiver.valider("arbeidsgiver[$index]"))
    }
}
