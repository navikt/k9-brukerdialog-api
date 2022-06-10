package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.LocalDate

typealias Opphold = Bosted

class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,
    private val landkode: String, // TODO: 09/06/2022 Burde bruke klassen Land
    private val landnavn: String,
    private val erEØSLand: Boolean? = null
) {
    companion object {
        internal fun List<Bosted>.somK9Bosteder() = Bosteder().medPerioder(this.associate { it.somK9Bosted() })
        internal fun List<Opphold>.somK9Utenlandsopphold() = Utenlandsopphold().medPerioder(this.associate { it.somK9Utenlandsopphold() })
        internal fun List<Bosted>.valider(felt: String) = this.flatMapIndexed { index, bosted ->
            bosted.valider("$felt[$index]")
        }
    }

    internal fun somK9Bosted() = Pair(Periode(fraOgMed, tilOgMed), Bosteder.BostedPeriodeInfo().medLand(Landkode.of(landkode)))

    internal fun somK9Utenlandsopphold() = Pair(Periode(fraOgMed, tilOgMed), Utenlandsopphold.UtenlandsoppholdPeriodeInfo().medLand(Landkode.of(landkode)))

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(erEØSLand, "$felt.erEØSLand må være satt")
        krever(!fraOgMed.isAfter(tilOgMed), "$felt.fraOgMed kan ikke være etter tilOgMed")
        krever(landnavn.isNotBlank(), "$felt.landnavn kan ikke være blankt eller tomt. landnavn='$landnavn'")
        krever(landkode.isNotBlank(), "$felt.landkode kan ikke være blankt eller tomt. landkode='$landkode'")
    }
}