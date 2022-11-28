package no.nav.k9brukerdialogapi.soknad

import no.nav.k9brukerdialogapi.ytelse.Ytelse

interface Søknad {
    fun ytelse(): Ytelse
    fun søknadId(): String
}
