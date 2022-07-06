package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Koronaoverføre.Companion.STENGINGSPERIODE_2021
import java.time.LocalDate
import kotlin.test.Test

class KoronaoverføreTest {

    @Test
    fun `Gyldig Koronaoverføring gir ingen feil`(){
        Koronaoverføre(5, STENGINGSPERIODE_2021).valider("koronaoverføre").verifiserIngenFeil()
        Koronaoverføre(1, STENGINGSPERIODE_2021).valider("koronaoverføre").verifiserIngenFeil()
        Koronaoverføre(999, STENGINGSPERIODE_2021).valider("koronaoverføre").verifiserIngenFeil()
    }

    @Test
    fun `Koronaoverføring av mindre enn 1 dag skal gi feil`(){
        Koronaoverføre(0, STENGINGSPERIODE_2021).valider("koronaoverføre")
            .verifiserFeil(1, listOf("koronaoverføre.antallDagerSomSkalOverføres må være innenfor range 1..999."))
    }

    @Test
    fun `Koronaoverføring av mer enn 999 dag skal gi feil`(){
        Koronaoverføre(1_000, STENGINGSPERIODE_2021).valider("koronaoverføre")
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