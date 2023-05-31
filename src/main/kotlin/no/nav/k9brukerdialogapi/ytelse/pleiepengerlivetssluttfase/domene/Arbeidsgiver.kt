package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class Arbeidsgiver(
    private val navn: String? = null,
    private val organisasjonsnummer: String,
    private val erAnsatt: Boolean,
    private val sluttetFørSøknadsperiode: Boolean? = null,
    private val arbeidsforhold: Arbeidsforhold? = null
) {

    companion object{
        internal fun List<Arbeidsgiver>.valider(felt: String = "arbeidsgivere") = flatMapIndexed { index: Int, arbeidsgiver: Arbeidsgiver ->
            arbeidsgiver.valider("$felt[$index]")
        }
        internal fun List<Arbeidsgiver>.somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) = map { it.somK9Arbeidstaker(fraOgMed, tilOgMed) }
    }

    internal fun valider(felt: String = "arbeidsgiver") = mutableListOf<String>().apply {
        krever(!navn.isNullOrBlank(), "$felt.navn kan ikke være null eller blankt.")
        krever(organisasjonsnummer.erGyldigOrganisasjonsnummer(), "$felt.organisasjonsnummer er ikke gyldig.")
        arbeidsforhold?.let { addAll(it.valider("$felt.arbeidsforhold")) }
    }

    fun somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) = Arbeidstaker().apply {
        medOrganisasjonsnummer(Organisasjonsnummer.of(this@Arbeidsgiver.organisasjonsnummer))
        medOrganisasjonsnavn(navn)
        medArbeidstidInfo(arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed))
    }

}
