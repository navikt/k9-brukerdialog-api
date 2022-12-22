package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.EKTEFELLE
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.SAMBOER
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Mottaker.SAMVÆRSFORELDER
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class FordeleTest {

    @Test
    fun `Gyldig Fordele gir ingen feil`(){
        Fordele(SAMVÆRSFORELDER).valider("fordele").verifiserIngenFeil()
    }

    @Test
    fun `Fordele til en annen mottaker enn SAMVÆRSFORELDER skal gi feil`(){
        Fordele(SAMBOER).valider("fordele").verifiserFeil(1, listOf("fordele.mottakerType må være 'SAMVÆRSFORELDER'."))
        Fordele(EKTEFELLE).valider("fordele").verifiserFeil(1, listOf("fordele.mottakerType må være 'SAMVÆRSFORELDER'."))
    }

    @Test
    fun `Fordele genererer forventet KomplettFordele med riktig vedleggId`(){
        val vedleggId = "12345abc"
        val fordele = Fordele(SAMVÆRSFORELDER, listOf(URL("http://localhost:8080/vedlegg/$vedleggId")))
        val komplettFordele = fordele.somKomplettFordele()
        assertEquals(komplettFordele.samværsavtaleVedleggId.first(), vedleggId)
    }
}
