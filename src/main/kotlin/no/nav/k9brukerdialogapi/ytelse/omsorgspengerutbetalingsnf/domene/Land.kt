package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9brukerdialogapi.general.krever
import java.util.*

class Land(
    private val landkode: String,
    private val landnavn: String
) {
    companion object{
        // ISO 3166 alpha-3 landkode - https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
        private val LANDKODER: MutableSet<String> = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3)
    }

    internal fun somK9Landkode() = Landkode.of(landkode)

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(LANDKODER.contains(landkode), "$felt.landkode '$landkode' er ikke en gyldig ISO 3166-1 alpha-3 kode.")
        krever(landnavn.isNotBlank(), "$felt.landnavn kan ikke være tomt eller blankt.")
    }
}