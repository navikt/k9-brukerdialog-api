package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class Søknad(
    val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val id: String,
    private val språk: String,
    private val annenForelder: AnnenForelder,
    private val barn: List<Barn> = listOf(),
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    internal fun tilK9Format(søker: Søker) = K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.somK9Søker(),
        OmsorgspengerMidlertidigAlene(
            barn.map { it.somK9Barn() },
            annenForelder.somK9AnnenForelder(),
            null
        )
    )

    internal fun tilKomplettSøknad(søker: Søker, k9Format: K9Søknad) = KomplettSøknad(
        mottatt = mottatt,
        søker = søker,
        søknadId = søknadId,
        id = id,
        språk = språk,
        annenForelder = annenForelder,
        barn = barn,
        k9Format = k9Format,
        harBekreftetOpplysninger = harBekreftetOpplysninger,
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
    )

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { barn ->
            if (barn.manglerIdentifikator()) barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        }
    }

    internal fun valider() = mutableListOf<String>().apply {
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(barn.isNotEmpty(), "Listen over barn kan ikke være tom")
        addAll(annenForelder.valider("annenForelder"))
        barn.forEachIndexed { index, barn -> addAll(barn.valider("barn[$index]")) }

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }
}