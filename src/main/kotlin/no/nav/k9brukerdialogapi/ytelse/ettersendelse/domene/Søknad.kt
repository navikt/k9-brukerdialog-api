package no.nav.k9brukerdialogapi.ytelse.ettersendelse.domene

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.ettersendelse.domene.Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE
import no.nav.k9brukerdialogapi.ytelse.ettersendelse.domene.Søknadstype.PLEIEPENGER_SYKT_BARN
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class Søknad(
    private val språk: String,
    private val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val vedlegg: List<URL>,
    private val beskrivelse: String? = null,
    private val søknadstype: Søknadstype,
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

        if((søknadstype == PLEIEPENGER_LIVETS_SLUTTFASE || søknadstype == PLEIEPENGER_SYKT_BARN) && beskrivelse.isNullOrBlank()){
            add(
                Violation(
                    parameterName = "beskrivelse",
                    parameterType = ParameterType.ENTITY,
                    reason = "Beskrivelse kan ikke være tom, null eller blank dersom det gjelder pleiepenger.",
                    invalidValue = beskrivelse
                )
            )
        }

        if (!harForståttRettigheterOgPlikter) {
            add(
                Violation(
                    parameterName = "harForståttRettigheterOgPlikter",
                    parameterType = ParameterType.ENTITY,
                    reason = "Må ha forstått rettigheter og plikter for å sende inn søknad."
                )
            )
        }

        if (!harBekreftetOpplysninger) {
            add(
                Violation(
                    parameterName = "harBekreftetOpplysninger",
                    parameterType = ParameterType.ENTITY,
                    reason = "Opplysningene må bekreftes for å sende inn søknad."
                )
            )
        }


        if (this.isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }
}