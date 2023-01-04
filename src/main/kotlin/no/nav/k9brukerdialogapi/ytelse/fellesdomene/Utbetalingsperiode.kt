package no.nav.k9brukerdialogapi.ytelse.fellesdomene

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
    private val årsak: FraværÅrsak? = null,
    private val aktivitetFravær: List<AktivitetFravær>
) {
    companion object{
        internal fun List<Utbetalingsperiode>.valider(felt: String) = this.flatMapIndexed { index, periode ->
            periode.valider("$felt[$index]")
        }

        internal fun List<Utbetalingsperiode>.somK9FraværPeriode(
            søknadÅrsak: SøknadÅrsak? = null,
            organisasjonsnummer: String? = null
        ) = this.map { it.somK9FraværPeriode(søknadÅrsak, organisasjonsnummer) }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(aktivitetFravær.isNotEmpty(), "$felt.aktivitetFravær kan ikke være tom.")
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

    internal fun somK9FraværPeriode(
        søknadÅrsak: SøknadÅrsak? = null,
        organisasjonsnummer: String? = null
    ) = FraværPeriode()
            .medPeriode(Periode(fraOgMed, tilOgMed))
            .medFraværÅrsak(årsak?.somK9FraværÅrsak() ?: K9FraværÅrsak.ORDINÆRT_FRAVÆR)
            .medSøknadsårsak(søknadÅrsak)
            .medAktivitetFravær(aktivitetFravær.map {it.somK9AktivitetFravær()})
            .medArbeidsgiverOrgNr(Organisasjonsnummer.of(organisasjonsnummer))
            .medNormalarbeidstid(antallTimerPlanlagt)
            .medFravær(antallTimerBorte)

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
