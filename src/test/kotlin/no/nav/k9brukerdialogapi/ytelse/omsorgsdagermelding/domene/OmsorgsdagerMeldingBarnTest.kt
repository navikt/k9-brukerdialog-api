package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarnTest {

    @Test
    fun `Oppdatering av identitetsnummer fungerer som forventet`(){
        val barn = Barn(
            identitetsnummer = null,
            fødselsdato = LocalDate.parse("2022-01-01"),
            navn = "Ola",
            aleneOmOmsorgen = true,
            utvidetRett = false
        )
        assertTrue(barn.manglerIdentitetsnummer())
        barn.oppdaterIdentitetsnummerMed("02119970078")
        assertFalse(barn.manglerIdentitetsnummer())
    }

    @Test
    fun `Gyldig barn gir ingen feil`(){
        Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            navn = "Ola",
            aleneOmOmsorgen = true,
            utvidetRett = false
        ).valider("barn").verifiserIngenFeil()
    }

    @Test
    fun `Barn uten identitetsnummer skal gi feil`(){
        Barn(
            identitetsnummer = null,
            fødselsdato = LocalDate.parse("2022-01-01"),
            navn = "Ola",
            aleneOmOmsorgen = true,
            utvidetRett = false
        ).valider("barn").verifiserFeil(1, listOf("barn.identitetsnummer kan ikke være null eller blank."))
    }

    @Test
    fun `Barn med tomt navn skal gi feil`(){
        Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            navn = " ",
            aleneOmOmsorgen = true,
            utvidetRett = false
        ).valider("barn").verifiserFeil(1, listOf("barn.navn kan ikke være tomt eller blank."))
    }

    @Test
    fun `Barn hvor aleneOmOmsorgen=null skal gi feil`(){
        Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            navn = "Ola",
            aleneOmOmsorgen = null,
            utvidetRett = false
        ).valider("barn").verifiserFeil(1, listOf("barn.aleneOmOmsorgen kan ikke være null. Må være true/false."))
    }

    @Test
    fun `Barn hvor utvidetRett=null skal gi feil`(){
        Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            navn = "Ola",
            aleneOmOmsorgen = true,
            utvidetRett = null
        ).valider("barn").verifiserFeil(1, listOf("barn.utvidetRett kan ikke være null. Må være true/false."))
    }
}