package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Barn.Companion.somK9BarnListe
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import kotlin.test.Test

class BarnTest {

    @Test
    fun `Gyldig barn er gyldig`() {
        val gyldigBarn = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2024-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen",
            type = TypeBarn.FRA_OPPSLAG
        )
        gyldigBarn.valider("barn").verifiserIngenFeil()
    }

    @Test
    fun `Navnløse barn er ikke gyldig`() {
        val noname = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2024-01-01"),
            aktørId = "12345",
            navn = "",
            type = TypeBarn.FRA_OPPSLAG
        )
        noname.valider("barn").verifiserFeil(1, listOf("barn.navn kan ikke være tomt eller blankt."))
    }

    @Test
    fun `Ufødte barn er ikke gyldig`() {
        val fremtidsbarn = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2999-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen",
            type = TypeBarn.FRA_OPPSLAG
        )
        fremtidsbarn.valider("barn").verifiserFeil(1, listOf("barn.fødselsdato kan ikke være i fremtiden."))
    }

    @Test
    fun `Personer over 18 år er ikke gyldige barn`() {
        val voksen = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("1987-01-01"),
            aktørId = "12345",
            navn = "Indre Barnesen",
            type = TypeBarn.FRA_OPPSLAG
        )
        voksen.valider("barn").verifiserFeil(1, listOf("barn.fødselsdato kan ikke være mer enn 19 år siden."))
    }

    @Test
    fun `K9Barn-listen skal kun inkludere fosterbarn`() {
        val listeMedBarn = listOf(
            Barn(
                identitetsnummer = "02119970078",
                fødselsdato = LocalDate.parse("2020-01-01"),
                aktørId = "12345",
                navn = "Van Li Barnesen",
                type = TypeBarn.FRA_OPPSLAG
            ),
            Barn(
                identitetsnummer = "02119970078",
                fødselsdato = LocalDate.parse("2021-01-01"),
                aktørId = "12345",
                navn = "Foster Barnesen",
                type = TypeBarn.FOSTERBARN
            ),
            Barn(
                identitetsnummer = "02119970078",
                fødselsdato = LocalDate.parse("2022-01-01"),
                aktørId = "12345",
                navn = "Anna Barnesen",
                type = TypeBarn.ANNET
            )
        )
        val k9BarnListe = listeMedBarn.somK9BarnListe()
        assertEquals(k9BarnListe.size, 1);
        assertEquals(k9BarnListe.get(0).fødselsdato, LocalDate.parse("2021-01-01"))
    }
}
