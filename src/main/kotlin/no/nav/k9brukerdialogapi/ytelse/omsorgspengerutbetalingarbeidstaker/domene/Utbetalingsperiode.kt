package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.fravær.AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.Duration
import java.time.LocalDate
import no.nav.k9.søknad.felles.fravær.FraværÅrsak as K9FraværÅrsak

class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,
    private val antallTimerBorte: Duration? = null,
    private val antallTimerPlanlagt: Duration? = null,
    private val årsak: FraværÅrsak
) {
    companion object{
        internal fun List<Utbetalingsperiode>.valider(felt: String) = this.flatMapIndexed { index, periode ->
            periode.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(!fraOgMed.isAfter(tilOgMed),"$felt.fraOgMed kan ikke være etter tilOgMed")
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

    internal fun somFraværPeriode(
        søknadÅrsak: SøknadÅrsak,
        aktivitetFravær: List<AktivitetFravær>,
        organisasjonsnummer: Organisasjonsnummer
    ) = FraværPeriode(
        Periode(fraOgMed, tilOgMed),
        antallTimerBorte,
        årsak.somK9FraværÅrsak(),
        søknadÅrsak,
        aktivitetFravær,
        organisasjonsnummer,
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
