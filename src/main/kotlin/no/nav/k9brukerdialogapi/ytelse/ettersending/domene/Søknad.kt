package no.nav.k9brukerdialogapi.ytelse.ettersending.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class Søknad(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val språk: String,
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    internal val vedlegg: List<URL>,
    private val beskrivelse: String? = null,
    internal val søknadstype: Søknadstype,
    private val harBekreftetOpplysninger: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean
) {

    internal fun valider() = mutableListOf<String>().apply {
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(vedlegg.isNotEmpty(), "Liste over vedlegg kan ikke være tom")
        if(søknadstype.gjelderPleiepenger()) krever(!beskrivelse.isNullOrBlank(), "beskrivelse må være satt dersom det gjelder pleiepenger")
        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    internal fun somKomplettSøknad(søker: Søker, k9Format: Ettersendelse, titler: List<String>) =
        KomplettSøknad(
            søker = søker,
            språk = språk,
            mottatt = mottatt,
            vedleggId = vedlegg.map { it.vedleggId() },
            søknadId = søknadId,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            beskrivelse = beskrivelse,
            søknadstype = søknadstype,
            titler = titler,
            k9Format = k9Format
        )

    fun somK9Format(søker: Søker) = Ettersendelse.builder()
        .søknadId(SøknadId(søknadId))
        .mottattDato(mottatt)
        .søker(søker.somK9Søker())
        .ytelse(søknadstype.somK9Ytelse())
        .build()
}