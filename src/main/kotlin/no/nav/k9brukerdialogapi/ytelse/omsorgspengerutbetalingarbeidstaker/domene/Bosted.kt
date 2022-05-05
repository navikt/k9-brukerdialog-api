package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate

typealias Opphold = Bosted

class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,
    private val landkode: String,
    private val landnavn: String,
    private val erEØSLand: Boolean? = null
){

    companion object{
        internal fun List<Bosted>.somK9Bosteder() = Bosteder().medPerioder(this.associate { it.somK9Bosted() })
        internal fun List<Bosted>.somK9Utenlandsopphold() = Utenlandsopphold().medPerioder(this.associate { it.somK9Utenlandsopphold() })
    }

    init {
        requireNotNull(erEØSLand) { "erEØSLand må være satt." }
        require(!fraOgMed.isAfter(tilOgMed)) { "fraOgMed kan ikke være etter tilOgMed," }
        require(landnavn.isNotBlank()) { "landnavn kan ikke være blankt eller tomt." }
        require(landkode.isNotBlank()) { "landkode kan ikke være blankt eller tomt." }
    }

    internal fun somK9Bosted() = Pair(Periode(fraOgMed, tilOgMed), Bosteder.BostedPeriodeInfo().medLand(Landkode.of(landkode)))
    internal fun somK9Utenlandsopphold() =
        Pair(Periode(fraOgMed, tilOgMed), Utenlandsopphold.UtenlandsoppholdPeriodeInfo().medLand(Landkode.of(landkode)))
}