package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn
import java.time.LocalDate
import kotlin.test.Test

class BarnTest {

    @Test
    fun `Gyldig barn gir ingen valideringsfeil`(){
        Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.parse("2022-01-01"),
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = null,
            utvidetRett = null,
            identitetsnummer = "26104500284"
        ).valider("barn").verifiserIngenFeil()
    }
}