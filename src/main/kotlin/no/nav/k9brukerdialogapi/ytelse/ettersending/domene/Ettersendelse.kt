package no.nav.k9brukerdialogapi.ytelse.ettersending.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.ettersendelse.EttersendelseValidator
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class Ettersendelse(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val språk: String,
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    internal val vedlegg: List<URL>,
    private val beskrivelse: String? = null,
    internal val søknadstype: Søknadstype,
    private val harBekreftetOpplysninger: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean
): Innsending {

    override fun valider() = mutableListOf<String>().apply {
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(vedlegg.isNotEmpty(), "Liste over vedlegg kan ikke være tom")
        if(søknadstype.gjelderPleiepenger()) krever(!beskrivelse.isNullOrBlank(), "beskrivelse må være satt dersom det gjelder pleiepenger")
        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    override fun somKomplettSøknad(søker: Søker, k9Format: no.nav.k9.søknad.Innsending?, titler: List<String>): KomplettEttersendelse {
        requireNotNull(k9Format)
        return KomplettEttersendelse(
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
            k9Format = k9Format as Ettersendelse
        )
    }

    override fun somK9Format(søker: Søker) = Ettersendelse.builder()
        .søknadId(SøknadId(søknadId))
        .mottattDato(mottatt)
        .søker(søker.somK9Søker())
        .ytelse(søknadstype.somK9Ytelse())
        .build()

    override fun ytelse(): Ytelse = Ytelse.ETTERSENDING

    override fun søknadId(): String = søknadId

    override fun vedlegg(): List<URL> = vedlegg

    override fun ettersendelseValidator(): SøknadValidator<Ettersendelse> = EttersendelseValidator()
}
