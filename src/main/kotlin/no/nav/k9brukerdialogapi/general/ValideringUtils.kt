package no.nav.k9brukerdialogapi.general

internal fun MutableList<String>.krever(resultat: Boolean?, feilmelding: String = "") {
    if (resultat != true) this.add(feilmelding)
}

internal fun MutableList<String>.kreverIkkeNull(verdi: Any?, feilmelding: String = "") {
    if (verdi == null) this.add(feilmelding)
}