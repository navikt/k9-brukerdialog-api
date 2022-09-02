package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Land
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.OpptjeningIUtlandet.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.OpptjeningType.ARBEIDSTAKER
import java.time.LocalDate
import kotlin.test.Test

class OpptjeningIUtlandetTest{

    @Test
    fun `Gyldig OpptjeningIUtlandet gir ingen valideringsfeil`(){
        OpptjeningIUtlandet(
            navn = "Fisk AS",
            opptjeningType = ARBEIDSTAKER,
            land = Land("NLD", "Nederland"),
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01")
        ).valider("opptjeningIUtlandet").verifiserIngenFeil()
    }

    @Test
    fun `Ugyldig land gir valideringsfeil`(){
        OpptjeningIUtlandet(
            navn = "Fisk AS",
            opptjeningType = ARBEIDSTAKER,
            land = Land("MARS", "  "),
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("opptjeningIUtlandet").verifiserFeil(2,
            listOf(
                "opptjeningIUtlandet.land.Landkode 'MARS' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "opptjeningIUtlandet.land.landnavn kan ikke være tomt eller blankt."
            )
        )
    }

    @Test
    fun `Liste med OpptjeningIUtlandet hvor fraOgMed er etter tilOgMed gir valideringsfeil`() {
        listOf(
            OpptjeningIUtlandet(
                "Fisk AS", ARBEIDSTAKER,
                Land("NLD", "Nederland"),
                LocalDate.parse("2022-01-10"),
                LocalDate.parse("2022-01-01")
            ),
            OpptjeningIUtlandet(
                "Fisk AS", ARBEIDSTAKER,
                Land("NLD", "Nederland"),
                LocalDate.parse("2022-01-10"),
                LocalDate.parse("2022-01-01")
            )
        ).valider("opptjeningIUtlandet").verifiserFeil(2,
            listOf(
                "opptjeningIUtlandet[0].tilOgMed må være lik eller etter fraOgMed.",
                "opptjeningIUtlandet[1].tilOgMed må være lik eller etter fraOgMed."
            )
        )
    }
}