package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.Duration
import java.time.LocalDate
import no.nav.k9.søknad.felles.fravær.AktivitetFravær as K9AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværÅrsak as K9FraværÅrsak

class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,
    private val antallTimerBorte: Duration? = null,
    private val antallTimerPlanlagt: Duration? = null,
    private val årsak: FraværÅrsak,
    // TODO: 08/06/2022 Vurder om frontend kan endre til å sende med liste med Arbeidstaker, da slipper vi spesiell håndtering at snf sender men ikke arbeidstaker.
    private val aktivitetFravær: List<AktivitetFravær> = listOf()
) {
    companion object{
        internal fun List<Utbetalingsperiode>.valider(felt: String) = this.flatMapIndexed { index, periode ->
            periode.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(tilOgMed.erLikEllerEtter(fraOgMed),"$felt.tilOgMed må være lik eller etter fraOgMed.")

        if(antallTimerBorte != null){
            kreverIkkeNull(antallTimerPlanlagt, "$felt.Dersom antallTimerBorte er satt må antallTimerPlanlagt være satt")
        }
        if(antallTimerPlanlagt != null){
            kreverIkkeNull(antallTimerBorte, "$felt.Dersom antallTimerPlanlagt er satt må antallTimerBorte være satt")
        }
        if(antallTimerPlanlagt != null && antallTimerBorte != null) {
            krever(antallTimerPlanlagt >= antallTimerBorte, "$felt.antallTimerBorte kan ikke være større enn antallTimerPlanlagt")
        }
    }

    internal fun somFraværPeriodeForArbeidstaker(
        søknadÅrsak: SøknadÅrsak,
        organisasjonsnummer: Organisasjonsnummer
    ) = FraværPeriode(
        Periode(fraOgMed, tilOgMed),
        antallTimerBorte,
        årsak.somK9FraværÅrsak(),
        søknadÅrsak,
        listOf(AktivitetFravær.ARBEIDSTAKER.somK9AktivitetFravær()),
        organisasjonsnummer,
        null
    )

    internal fun somFraværPeriode(
        søknadÅrsak: SøknadÅrsak,
    ) = FraværPeriode(
        Periode(fraOgMed, tilOgMed),
        antallTimerBorte,
        årsak.somK9FraværÅrsak(),
        søknadÅrsak,
        aktivitetFravær.map {
            it.somK9AktivitetFravær()
        },
        null,
        null
    )
}

enum class FraværÅrsak {
    STENGT_SKOLE_ELLER_BARNEHAGE,
    SMITTEVERNHENSYN,
    ORDINÆRT_FRAVÆR;

    fun somK9FraværÅrsak() = when(this){
        STENGT_SKOLE_ELLER_BARNEHAGE -> K9FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE
        SMITTEVERNHENSYN -> K9FraværÅrsak.SMITTEVERNHENSYN
        ORDINÆRT_FRAVÆR -> K9FraværÅrsak.ORDINÆRT_FRAVÆR
    }
}

enum class AktivitetFravær {
    ARBEIDSTAKER,
    FRILANSER,
    SELVSTENDIG_VIRKSOMHET;

    fun somK9AktivitetFravær() = when(this){
        ARBEIDSTAKER -> K9AktivitetFravær.ARBEIDSTAKER
        FRILANSER -> K9AktivitetFravær.FRILANSER
        SELVSTENDIG_VIRKSOMHET -> K9AktivitetFravær.SELVSTENDIG_VIRKSOMHET
    }
}