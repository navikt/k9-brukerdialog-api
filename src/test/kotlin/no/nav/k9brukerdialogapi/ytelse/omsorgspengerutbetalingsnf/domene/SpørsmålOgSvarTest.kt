package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SpørsmålOgSvar.Companion.valider
import kotlin.test.Test

class SpørsmålOgSvarTest {

    @Test
    fun `Gyldig SpørsmålOgSvar gir ingen feil`(){
        SpørsmålOgSvar(
            spørsmål = "Har du hund?",
            svar = true
        ).valider("SpørsmålOgSvar").verifiserIngenFeil()
    }

    @Test
    fun `Spørsmål som blank gir feil`(){
        SpørsmålOgSvar(
            spørsmål = " ",
            svar = true
        ).valider("SpørsmålOgSvar").verifiserFeil(1, listOf("SpørsmålOgSvar.spørsmål kan ikke være tomt eller blankt."))
    }

    @Test
    fun `Spørsmål over 1000 tegn gir feil`(){
        SpørsmålOgSvar(
            spørsmål = "A".repeat(1001),
            svar = true
        ).valider("SpørsmålOgSvar").verifiserFeil(1, listOf("SpørsmålOgSvar.spørsmål kan ikke være mer enn 1000 tegn."))
    }

    @Test
    fun `Svar som null gir feil`(){
        SpørsmålOgSvar(
            spørsmål = "Har du hund?",
            svar = null
        ).valider("SpørsmålOgSvar").verifiserFeil(1, listOf("SpørsmålOgSvar.svar kan ikke være null. Må være true/false."))
    }

    @Test
    fun `Liste med SpørsmålOgSvar hvor svar er null gir feil`() {
        listOf(
            SpørsmålOgSvar(
                spørsmål = "Har du en hund?",
                svar = null
            ), SpørsmålOgSvar(
                spørsmål = "Har du katt?",
                svar = null
            )
        ).valider("SpørsmålOgSvar").toMutableList().verifiserFeil(
            2, listOf(
                "SpørsmålOgSvar[0].svar kan ikke være null. Må være true/false.",
                "SpørsmålOgSvar[1].svar kan ikke være null. Må være true/false."
            )
        )
    }
}