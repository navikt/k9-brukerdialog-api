package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import java.net.URL
import java.util.*

class Søknad(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val språk: String,
    private val vedlegg: List<URL>,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val bekreftelser: Bekreftelser,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null
){
    init {
        require(arbeidsgivere.isNotEmpty()) { "Må ha minst en arbeidsgiver satt." }
    }
}

class Bekreftelser(
    val harBekreftetOpplysninger: Boolean? = null,
    val harForståttRettigheterOgPlikter: Boolean? = null
){
    init {
        requireNotNull(harBekreftetOpplysninger)
        requireNotNull(harForståttRettigheterOgPlikter)
    }
}