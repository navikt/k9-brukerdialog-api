package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.*
import kotlin.test.Test

class OverføreTest {

    @Test
    fun `Gyldig overføring gir ingen feil`(){
        Overføre(SAMBOER, 5).valider("overføre").verifiserIngenFeil()
        Overføre(SAMBOER, 1).valider("overføre").verifiserIngenFeil()
        Overføre(SAMBOER, 10).valider("overføre").verifiserIngenFeil()
    }

    @Test
    fun `Overføring til SAMVÆRSFORELDER skal gi feil`(){
        Overføre(SAMVÆRSFORELDER, 5).valider("overføre")
            .verifiserFeil(1, listOf("overføre.mottakerType må være en av [SAMBOER, EKTEFELLE]."))
    }

    @Test
    fun `Overføring av mindre enn 1 dag skal gi feil`(){
        Overføre(EKTEFELLE, 0).valider("overføre")
            .verifiserFeil(1, listOf("overføre.antallDagerSomSkalOverføres må være innenfor range 1..10."))
    }

    @Test
    fun `Overføring av mer enn 10 dag skal gi feil`(){
        Overføre(EKTEFELLE, 11).valider("overføre")
            .verifiserFeil(1, listOf("overføre.antallDagerSomSkalOverføres må være innenfor range 1..10."))
    }

}