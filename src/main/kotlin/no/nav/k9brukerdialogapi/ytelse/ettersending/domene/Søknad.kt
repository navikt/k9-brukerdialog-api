package no.nav.k9brukerdialogapi.ytelse.ettersending.domene

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.validerSamtykke
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

    internal fun valider() = mutableSetOf<Violation>().apply {
        if(vedlegg.isEmpty()){
            add(
                Violation(
                    parameterName = "vedlegg",
                    parameterType = ParameterType.ENTITY,
                    reason = "Liste over vedlegg kan ikke være tom.",
                    invalidValue = vedlegg
                )
            )
        }

        if(søknadstype.gjelderPleiepenger() && beskrivelse.isNullOrBlank()){
            add(
                Violation(
                    parameterName = "beskrivelse",
                    parameterType = ParameterType.ENTITY,
                    reason = "Beskrivelse kan ikke være tom, null eller blank dersom det gjelder pleiepenger.",
                    invalidValue = beskrivelse
                )
            )
        }

        addAll(validerSamtykke(harForståttRettigheterOgPlikter, harBekreftetOpplysninger))
        if (this.isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }
}