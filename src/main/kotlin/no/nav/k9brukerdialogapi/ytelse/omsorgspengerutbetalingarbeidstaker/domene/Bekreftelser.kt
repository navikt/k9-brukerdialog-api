package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

class Bekreftelser(
    val harBekreftetOpplysninger: Boolean? = null,
    val harForståttRettigheterOgPlikter: Boolean? = null
){
    init {
        requireNotNull(harBekreftetOpplysninger)
        requireNotNull(harForståttRettigheterOgPlikter)
    }
}