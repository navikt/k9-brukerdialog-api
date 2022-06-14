package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import kotlin.test.Test

class LandTest {

    @Test
    fun `Gyldig Land gir ingen valideringsfeil`(){
        Land(landkode = "NLD", landnavn = "Nederland").valider("land").verifiserIngenFeil()
    }

    @Test
    fun `Land med ugydlig landnavn gir valideringsfeil`(){
        Land(landkode = "NLD", landnavn = " ")
            .valider("land").verifiserFeil(1, listOf("land.landnavn kan ikke være tomt eller blankt."))
    }

    @Test
    fun `Land med ugydlig landkode gir valideringsfeil`(){
        Land(landkode = "AAA", landnavn = "Nederland")
            .valider("land").verifiserFeil(1, listOf("land.Landkode 'AAA' er ikke en gyldig ISO 3166-1 alpha-3 kode."))
    }
}