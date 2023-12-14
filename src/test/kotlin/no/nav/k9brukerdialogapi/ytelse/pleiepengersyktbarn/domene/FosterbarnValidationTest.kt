package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.BarnDetaljer
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.ÅrsakManglerIdentitetsnummer
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FosterbarnValidationTest {
    private companion object {
        val felt = "barn"
        val gyldigBarn = BarnDetaljer(
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2021-01-01"),
            aktørId = "10000001",
            navn = "Barnesen",
            årsakManglerIdentitetsnummer = null
        )
    }

    @Test
    fun `Gyldig barn gir ingen feil`(){
        gyldigBarn.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Når AktørId settes som ID på barnet kreves hverken relasjon til barnet eller navn`() {
        val barn = gyldigBarn.copy(
            aktørId = "10000001",
            navn = null
        )
        barn.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Skal ikke gi feil selvom fødselsnummer er null så lenge fødselsdato og årsak er satt`() {
        val barn = gyldigBarn.copy(
            fødselsnummer = null,
            fødselsdato = LocalDate.parse("2021-01-01"),
            årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT
        )
        barn.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Skal gi feil dersom fødselsnummer ikke settes og man ikke har satt fødsesldato og årsak`() {
        val barn = gyldigBarn.copy(
            fødselsnummer = null,
            årsakManglerIdentitetsnummer = null,
            fødselsdato = null
        )
        barn.valider(felt).assertFeilPå(listOf("barn.fødselsdato", "barn.årsakManglerIdentitetsnummer"))
    }

    @Test
    fun `Skal gi feil dersom fødselsdato er i fremtiden`() {
        val barn = gyldigBarn.copy(
            fødselsdato = LocalDate.now().plusDays(1)
        )
        barn.valider(felt).assertFeilPå(listOf("barn.fødselsdato"))
    }

}
