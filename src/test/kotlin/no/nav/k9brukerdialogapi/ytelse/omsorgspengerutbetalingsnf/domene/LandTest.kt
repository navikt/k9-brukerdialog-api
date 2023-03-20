package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class LandTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "NLD", "SWE", "DNK", "NOR", "FIN", "ISL", "GBR", "FRA", "DEU", "BEL", "LUX", "ITA", "ESP",
            "PRT", "GRC", "CYP", "MLT", "AUT", "HUN", "SVK", "SVN", "CZE", "POL", "EST", "LVA", "LTU", "BLR", "UKR", "MDA",
            "ROU", "BGR", "HRV", "BIH", "SRB", "MNE", "ALB", "MKD", "GEO", "ARM", "AZE", "TUR", "CYP", "GIB", "GGY", "JEY",
            "IMN", "LIE", "CHE", "XXK"
        ]
    )
    fun `Gyldig Land gir ingen valideringsfeil`(landkode: String) {
        Land(landkode = landkode, landnavn = "Uviktig").valider("land").verifiserIngenFeil()
    }

    @Test
    fun `Land med ugydlig landnavn gir valideringsfeil`() {
        Land(landkode = "NLD", landnavn = " ")
            .valider("land").verifiserFeil(1, listOf("land.landnavn kan ikke være tomt eller blankt."))
    }

    @Test
    fun `Land med ugydlig landkode gir valideringsfeil`() {
        Land(landkode = "AAA", landnavn = "Nederland")
            .valider("land").verifiserFeil(1, listOf("land.landkode 'AAA' er ikke en gyldig ISO 3166-1 alpha-3 kode."))
    }
}
