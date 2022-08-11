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
            erYrkesaktiv = true,
            arbeiderINorge = true,
            arbeidssituasjon = listOf(ARBEIDSTAKER),
            type = Meldingstype.KORONA,
            korona = Koronaoverføre(4),
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
                erYrkesaktiv = true,
                arbeiderINorge = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                type = Meldingstype.KORONA,
                korona = Koronaoverføre(4),
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
                erYrkesaktiv = true,
                arbeiderINorge = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                type = Meldingstype.KORONA,
                korona = Koronaoverføre(4),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("mottakerFnr er ikke gyldig identifikator, '123abc*****'. Forventet at personidentifikator kun var siffer, men var 123abc****** (6)") }
            assertTrue { it.message.toString().contains("mottakerNavn kan ikke være tomt eller blankt.") }
        }
    }

    @Test
    fun `Melding hvor arbeidssituasjon og barn er tom liste skal gi feil`(){
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                arbeidssituasjon = listOf(),
                barn = listOf(),
                harUtvidetRett = true,
                harAleneomsorg = true,
                erYrkesaktiv = true,
                arbeiderINorge = true,
                type = Meldingstype.KORONA,
                korona = Koronaoverføre(4),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("arbeidssituasjon kan ikke være en tom liste.") }
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
                erYrkesaktiv = true,
                arbeiderINorge = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                type = Meldingstype.KORONA,
                korona = Koronaoverføre(4),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("barn[0].aleneOmOmsorgen kan ikke være null. Må være true/false.") }
            assertTrue { it.message.toString().contains("barn[0].utvidetRett kan ikke være null. Må være true/false.") }
        }
    }

    @Test
    fun `Melding hvor harAleneomsorg, harUtvidetRett, arbeiderINorge og erYrkesaktiv er null skal gi feil`(){
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
                erYrkesaktiv = null,
                arbeiderINorge = null,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                type = Meldingstype.KORONA,
                korona = Koronaoverføre(4),
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("harUtvidetRett kan ikke være null. Må være true/false.") }
            assertTrue { it.message.toString().contains("harAleneomsorg kan ikke være null. Må være true/false.") }
            assertTrue { it.message.toString().contains("arbeiderINorge kan ikke være null. Må være true/false.") }
            assertTrue { it.message.toString().contains("erYrkesaktiv kan ikke være null. Må være true/false.") }
        }
    }

    @Test
    fun `Melding hvor type=KORONA men korona er null skal gi feil`() {
        assertThrows<Throwblem> {
            Melding(
                id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                språk = "nb",
                mottakerFnr = "26104500284",
                mottakerNavn = "Navnesen",
                barn = listOf(
                    Barn(
                        identitetsnummer = "02119970078",
                        fødselsdato = LocalDate.now(),
                        navn = "Navnesen",
                        aleneOmOmsorgen = true,
                        utvidetRett = true
                    )
                ),
                harUtvidetRett = true,
                harAleneomsorg = true,
                erYrkesaktiv = true,
                arbeiderINorge = true,
                arbeidssituasjon = listOf(ARBEIDSTAKER),
                type = Meldingstype.KORONA,
                korona = null,
                harForståttRettigheterOgPlikter = true,
                harBekreftetOpplysninger = true
            ).valider()
        }.also {
            assertTrue { it.message.toString().contains("Ved type=KORONA kan ikke 'korona' være null.") }
        }
    }
}