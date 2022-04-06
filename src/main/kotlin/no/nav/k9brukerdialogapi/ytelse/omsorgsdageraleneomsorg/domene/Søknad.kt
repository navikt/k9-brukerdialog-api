package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorg
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.validerSamtykke
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.validerK9Format
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class Søknad(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val barn: List<Barn> = listOf(),
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {
    internal fun gjelderFlereBarn() = barn.size > 1

    internal fun manglerIdentidifaktorPåBarn() = barn.any { it.manglerIdentifikator() }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    internal fun splittTilEgenSøknadPerBarn(): List<Søknad> {
        return barn.map {
            Søknad(
                mottatt = this.mottatt,
                språk = this.språk,
                barn = listOf(it),
                harForståttRettigheterOgPlikter = this.harForståttRettigheterOgPlikter,
                harBekreftetOpplysninger = this.harBekreftetOpplysninger
            )
        }
    }

    internal fun somK9Format(søker: Søker): K9Søknad {
        // Innsendt søknad blir splittet opp i 1 søknad per barn. Derfor skal det kun være et barn i lista.
        require(barn.size == 1) { "Søknad etter splitt kan kun inneholdet et barn" }

        return K9Søknad()
            .medSøknadId(SøknadId(søknadId))
            .medMottattDato(mottatt)
            .medVersjon(Versjon.of("1.0.0"))
            .medSøker(søker.somK9Søker())
            .medYtelse(
                OmsorgspengerAleneOmsorg(
                    barn.first().somK9Barn(),
                    Periode(barn.first().k9PeriodeFraOgMed(), null),
                    ""
                )
            )
    }

    internal fun valider(): Set<Violation> = mutableSetOf<Violation>().apply {
        addAll(validerSamtykke(harForståttRettigheterOgPlikter, harBekreftetOpplysninger))
        barn.forEach { addAll(it.valider()) }

        if (barn.isEmpty()) {
            add(
                Violation(
                    parameterName = "barn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Listen over barn kan ikke være tom",
                    invalidValue = barn
                )
            )
        }

        if (this.isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    internal fun somKomplettSøknad(søker: Søker): KomplettSøknad {
        require(barn.size == 1) { "Søknad etter splitt kan kun inneholdet et barn" }
        val k9Format = this.somK9Format(søker).also { validerK9Format(it) }
        return KomplettSøknad(
            mottatt = mottatt,
            søker = søker,
            søknadId = søknadId,
            språk = språk,
            barn = this.barn[0],
            k9Søknad = k9Format,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )
    }
}