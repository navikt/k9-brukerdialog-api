package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Duration
import java.time.LocalDate

class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,
    private val antallTimerBorte: Duration? = null,
    private val antallTimerPlanlagt: Duration? = null
) {
    init {
        require(!fraOgMed.isAfter(tilOgMed)) { "fraOgMed kan ikke være etter tilOgMed," }
        if(antallTimerPlanlagt != null){
            requireNotNull(antallTimerBorte) { "Dersom antallTimerPlanlagt er satt må ikke antallTimerBorte være satt." }
            require(antallTimerPlanlagt >= antallTimerBorte) { "antallTimerBorte kan ikke være større enn antallTimerPlanlagt" }
        }
        if(antallTimerBorte != null){
            requireNotNull(antallTimerPlanlagt) { "Dersom antallTimerBorte er satt må ikke antallTimerPlanlagt være satt." }
        }
    }

}
