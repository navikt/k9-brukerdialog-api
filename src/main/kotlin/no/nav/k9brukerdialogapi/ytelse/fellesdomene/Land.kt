package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9brukerdialogapi.general.krever
import java.util.*

data class Land(
    val landkode: String,
    val landnavn: String
) {
    companion object{
        // ISO 3166 alpha-3 landkode - https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
        internal val LANDKODER: MutableSet<String> = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).toMutableSet().also {
            it.add("XXK") // Kode for "Kosovo
        }
    }

    override fun equals(other: Any?) = this === other || other is Land && this.equals(other)
    private fun equals(other: Land) = this.landkode == other.landkode && this.landnavn == other.landnavn
    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(landkode.isNotBlank(), "$felt.landkode kan ikke være blank.")
        krever(LANDKODER.contains(landkode), "$felt.landkode '$landkode' er ikke en gyldig ISO 3166-1 alpha-3 kode.")
        krever(landnavn.isNotBlank(), "$felt.landnavn kan ikke være tomt eller blankt.")
    }

    fun somK9Landkode() = Landkode.of(landkode)
}
