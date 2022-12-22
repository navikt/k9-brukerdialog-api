package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.assertFeilPå
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.assertIngenFeil
import java.time.LocalDate
import kotlin.test.Test

class VirksomhetTest {

    private companion object {
        val felt = "sn"

        private fun gyldigVirksomhet(
            næringstype: Næringstype = Næringstype.ANNEN,
            fiskerErPåBladB: Boolean = false,
            fraOgMed: LocalDate = LocalDate.now().minusDays(1),
            tilOgMed: LocalDate = LocalDate.now(),
            næringsinntekt: Int = 1111,
            navnPåVirksomheten: String = "TullOgTøys",
            registrertINorge: Boolean = true,
            registrertIUtlandet: Land? = null,
            organisasjonsnummer: String? = "101010",
            yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
            harFlereAktiveVirksomheter: Boolean? = true,
            erNyoppstartet: Boolean = true
        ) = Virksomhet(
            næringstype = næringstype,
            fiskerErPåBladB = fiskerErPåBladB,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            næringsinntekt = næringsinntekt,
            navnPåVirksomheten =navnPåVirksomheten,
            registrertINorge = registrertINorge,
            registrertIUtlandet = registrertIUtlandet,
            organisasjonsnummer = organisasjonsnummer,
            yrkesaktivSisteTreFerdigliknedeÅrene = yrkesaktivSisteTreFerdigliknedeÅrene,
            harFlereAktiveVirksomheter = harFlereAktiveVirksomheter,
            erNyoppstartet = erNyoppstartet
        )
    }

    @Test
    fun `FraOgMed kan ikke være før tilOgMed, validate skal returnere en violation`(){
        val virksomhet = gyldigVirksomhet(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(1),
        )
        virksomhet.valider(felt).assertFeilPå(listOf("virksomhet.tilogmed og virksomhet.fraogmed"))
    }

    @Test
    fun `FraOgMed er før tilogmed, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet(
            fraOgMed = LocalDate.now().minusDays(1),
            tilOgMed = LocalDate.now()
        )
        virksomhet.valider(felt).assertIngenFeil()
    }

    @Test
    fun `FraOgMed er lik tilogmed, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now(),
        )
        virksomhet.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så må orgnummer være satt, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet(
            registrertINorge = true,
            organisasjonsnummer = "101010",
        )
        virksomhet.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så skal den feile hvis orgnummer ikke er satt, validate skal returnere en violation`(){
        val virksomhet = gyldigVirksomhet(
            organisasjonsnummer = null,
            registrertINorge = true,
        )
        virksomhet.valider(felt).assertFeilPå(listOf("selvstendingNæringsdrivende.virksomhet.organisasjonsnummer"))
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må registrertIUtlandet være satt til noe, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet(
            registrertINorge = false,
            registrertIUtlandet = Land(
                landkode = "DEU",
                landnavn = "Tyskland"
            )
        )
        virksomhet.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må den feile hvis registrertIUtlandet ikke er satt til null, validate skal returnere en violation`(){
        val virksomhet = gyldigVirksomhet(
            registrertINorge = false,
            registrertIUtlandet = null
        )
        virksomhet.valider(felt).assertFeilPå(listOf("selvstendingNæringsdrivende.virksomhet.registrertIUtlandet"))
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode, validering skal gi feil`(){
        val virksomhet = gyldigVirksomhet(
            registrertINorge = false,
            registrertIUtlandet = Land(
                landnavn = "Tyskland",
                landkode = "NO"
            )
        )
        virksomhet.valider(felt).assertFeilPå(listOf("selvstendingNæringsdrivende.virksomhet.registrertIUtlandet.landkode"))
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode`(){
        val virksomhet = gyldigVirksomhet(
            registrertINorge = false,
            registrertIUtlandet = Land(
                landnavn = "Tyskland",
                landkode = "DEU"
            )
        )
        virksomhet.valider(felt).assertIngenFeil()
    }

    @Test
    fun `Hvis harFlereAktiveVirksomheter er null skal validering gi feil`(){
        val virksomhet = gyldigVirksomhet(
            harFlereAktiveVirksomheter = null
        )
        virksomhet.valider(felt).assertFeilPå(listOf("selvstendingNæringsdrivende.virksomhet.harFlereAktiveVirksomheter"))
    }
}

