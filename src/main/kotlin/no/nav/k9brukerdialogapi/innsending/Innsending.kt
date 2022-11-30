package no.nav.k9brukerdialogapi.innsending

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import java.net.URL
import no.nav.k9.søknad.Søknad as K9Søknad

interface Innsending {
    fun ytelse(): Ytelse
    fun søknadId(): String
    fun inneholderVedlegg(): Boolean = vedlegg().isNotEmpty()
    fun vedlegg(): List<URL>
    fun somK9Format(søker: Søker? = null): K9Søknad? = null

    @kotlin.jvm.Throws(Throwblem::class)
    fun valider(): List<String>
    fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Søknad? = null,
        titler: List<String> = listOf()
    ): KomplettInnsending

    fun validator(): SøknadValidator<K9Søknad>? = null
}
