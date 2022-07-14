package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Arbeidssituasjon.ARBEIDSTAKER
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class MeldingTest {

    @Test
    fun `Gyldig melding gir ingen feil`(){
        Melding(
            id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
            språk = "nb",
            mottakerFnr = "26104500284",
            mottakerNavn = "Navnesen",
            barn = listOf(Barn(
                identitetsnummer = "02119970078",
                fødselsdato = LocalDate.now(),
                navn = "Navnesen",
                aleneOmOmsorgen = true,
                utvidetRett = true
            )),
            harUtvidetRett = true,
            harAleneomsorg = true,
            arbeidssituasjon = listOf(ARBEIDSTAKER),
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        ).valider()
    }

    @Test
    fun `Melding hvor harBekreftetOpplysninger og harForståttRettigheterOgPlikter er false gir feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                barn = listOf(Barn(
                    identitetsnummer = "02119970078",
                    fødselsdato = LocalDate.now(),
                    navn = "Navnesen",
                    aleneOmOmsorgen = true,
                    utvidetRett = true
                )),
                harUtvidetRett = true,
                harAleneomsorg = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = false
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("harForståttRettigheterOgPlikter må være true.") }
            assertTrue { it.message.toString().contains("harBekreftetOpplysninger må være true.") }
        }
    }

    @Test
    fun `Melding hvor mottakerFnr og navn er ugyldig skal gi feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "123abc",
                mottakerNavn = "",
                barn = listOf(Barn(
                    identitetsnummer = "02119970078",
                    fødselsdato = LocalDate.now(),
                    navn = "Navnesen",
                    aleneOmOmsorgen = true,
                    utvidetRett = true
                )),
                harUtvidetRett = true,
                harAleneomsorg = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("mottakerFnr er ikke gyldig identifikator, '123abc*****'. Forventet at personidentifikator kun var siffer, men var 123abc****** (6)") }
            assertTrue { it.message.toString().contains("mottakerNavn kan ikke være tomt eller blankt.") }
        }
    }

    @Test
    fun `Melding hvor arbeidssituasjon er tom liste skal gi feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                barn = listOf(Barn(
                    identitetsnummer = "02119970078",
                    fødselsdato = LocalDate.now(),
                    navn = "Navnesen",
                    aleneOmOmsorgen = true,
                    utvidetRett = true
                )),
                arbeidssituasjon = listOf(),
                harUtvidetRett = true,
                harAleneomsorg = true,
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("arbeidssituasjon kan ikke være en tom liste.") }
        }
    }

    @Test
    fun `Melding hvor barn er tom liste skal gi feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                barn = listOf(),
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                harUtvidetRett = true,
                harAleneomsorg = true,
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("barn kan ikke være en tom liste.") }
        }
    }

    @Test
    fun `Melding hvor barn er ugyldig skal gi feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                barn = listOf(Barn(
                    identitetsnummer = "26104500284",
                    fødselsdato = LocalDate.now(),
                    navn = "Navnesen",
                    aleneOmOmsorgen = null,
                    utvidetRett = null
                )),
                harUtvidetRett = true,
                harAleneomsorg = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("barn[0].aleneOmOmsorgen kan ikke være null. Må være true/false.") }
            assertTrue { it.message.toString().contains("barn[0].utvidetRett kan ikke være null. Må være true/false.") }
        }
    }

    @Test
    fun `Melding hvor harAleneomsorg og harUtvidetRett er null skal gi feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                barn = listOf(Barn(
                    identitetsnummer = "02119970078",
                    fødselsdato = LocalDate.now(),
                    navn = "Navnesen",
                    aleneOmOmsorgen = true,
                    utvidetRett = true
                )),
                harUtvidetRett = null,
                harAleneomsorg = null,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("harUtvidetRett kan ikke være null. Må være true/false.") }
            assertTrue { it.message.toString().contains("harAleneomsorg kan ikke være null. Må være true/false.") }
        }
    }
}