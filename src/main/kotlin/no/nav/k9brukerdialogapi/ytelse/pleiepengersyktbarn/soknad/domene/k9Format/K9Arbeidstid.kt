package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format

import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Arbeidsgiver
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Søknad
import java.time.DayOfWeek
import java.time.LocalDate

internal fun Søknad.byggK9Arbeidstid(): Arbeidstid {
    val arbeidstid = Arbeidstid().apply {

        if(arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(fraOgMed, tilOgMed))

        medFrilanserArbeidstid(frilans.k9ArbeidstidInfo(fraOgMed, tilOgMed))
        selvstendigNæringsdrivende.arbeidsforhold?.let {
            medSelvstendigNæringsdrivendeArbeidstidInfo(selvstendigNæringsdrivende.k9ArbeidstidInfo(fraOgMed, tilOgMed))
        }
    }
    return arbeidstid
}

fun List<Arbeidsgiver>.tilK9Arbeidstaker(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate
): List<Arbeidstaker> {
    return this.map {
            Arbeidstaker()
                .medOrganisasjonsnummer(Organisasjonsnummer.of(it.organisasjonsnummer))
                .medOrganisasjonsnavn(it.navn)
                .medArbeidstidInfo(it.k9ArbeidstidInfo(fraOgMed, tilOgMed))
    }
}

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> = datesUntil(tilOgMed.plusDays(1))
    .toList()
    .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }
