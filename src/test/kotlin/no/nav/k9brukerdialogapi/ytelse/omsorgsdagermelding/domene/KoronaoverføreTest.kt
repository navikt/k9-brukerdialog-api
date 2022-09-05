package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Koronaoverføre.Companion.STENGINGSPERIODE_2021
import java.time.LocalDate
import kotlin.test.Test

class KoronaoverføreTest {

    @Test
    fun `Gyldig Koronaoverføring gir ingen feil`(){
        Koronaoverføre(1, STENGINGSPERIODE_2021).valider("koronaoverføre").verifiserIngenFeil()
        Koronaoverføre(5).valider("koronaoverføre").verifiserIngenFeil()
        Koronaoverføre(999).valider("koronaoverføre").verifiserIngenFeil()
    }

    @Test
    fun `Koronaoverføring av mindre enn 1 dag skal gi feil`(){
        Koronaoverføre(0).valider("koronaoverføre")
            .verifiserFeil(1, listOf("koronaoverføre.antallDagerSomSkalOverføres må være innenfor range 1..999."))
    }

    @Test
    fun `Koronaoverføring av mer enn 999 dag skal gi feil`(){
        Koronaoverføre(1_000).valider("koronaoverføre")
            .verifiserFeil(1, listOf("koronaoverføre.antallDagerSomSkalOverføres må være innenfor range 1..999."))
    }

    @Test
    fun `Koronaoverføring med ugyldig stengingsperiode skal gi feil`() {
        Koronaoverføre(
            5,
            KoronaStengingsperiode(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-12-31"))
        )
            .valider("koronaoverføre")
            .verifiserFeil(1, listOf("koronaoverføre.stengingsperiode må være en av [KoronaStengingsperiode(fraOgMed=2021-01-01, tilOgMed=2021-12-31)]."))
    }
}