package no.nav.k9brukerdialogapi.general

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue


internal fun MutableList<String>.krever(resultat: Boolean?, feilmelding: String = "") {
    if (resultat != true) this.add(feilmelding)
}

internal fun MutableList<String>.kreverIkkeNull(verdi: Any?, feilmelding: String = "") {
    if (verdi == null) this.add(feilmelding)
}

internal fun <E> MutableList<E>.validerFeil(antallFeil: Int) {
    assertEquals(antallFeil, this.size)
}

internal fun MutableList<String>.validerIngenFeil() {
    assertTrue(this.isEmpty())
}