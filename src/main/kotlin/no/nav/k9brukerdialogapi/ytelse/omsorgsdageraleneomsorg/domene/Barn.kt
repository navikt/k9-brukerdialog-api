package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    private val navn: String,
    private val aktørId: String,
    private var identitetsnummer: String? = null,
    private val tidspunktForAleneomsorg: TidspunktForAleneomsorg,
    private val dato: LocalDate? = null
) {
    fun manglerIdentifikator() = identitetsnummer.isNullOrBlank()

    fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(manglerIdentifikator()) identitetsnummer = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    fun somK9Barn() =  K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))

    fun k9PeriodeFraOgMed() = when (tidspunktForAleneomsorg) {
        TidspunktForAleneomsorg.SISTE_2_ÅRENE -> dato
        TidspunktForAleneomsorg.TIDLIGERE -> LocalDate.parse("${LocalDate.now().year.minus(1)}-01-01")
    }

    fun valider(): Set<Violation> = mutableSetOf<Violation>().apply {
        if (manglerIdentifikator() || (!identitetsnummer!!.erGyldigFodselsnummer())) {
            add(
                Violation(
                    parameterName = "barn.identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig identitetsnummer.",
                    invalidValue = identitetsnummer
                )
            )
        }

        if (navn.isBlank() || (navn.length > 100)) {
            add(
                Violation(
                    parameterName = "barn.navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navn på barnet kan ikke være tomt, og kan maks være 100 tegn.",
                    invalidValue = navn
                )
            )
        }

        if(tidspunktForAleneomsorg == TidspunktForAleneomsorg.SISTE_2_ÅRENE && dato == null){
            add(
                Violation(
                    parameterName = "barn.dato",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.dato kan ikke være tom dersom tidspunktForAleneomsorg er ${TidspunktForAleneomsorg.SISTE_2_ÅRENE.name}",
                    invalidValue = dato
                )
            )
        }
    }

    override fun equals(other: Any?) = this === other || other is Barn && this.equals(other)
    private fun equals(other: Barn) = this.identitetsnummer == other.identitetsnummer
}

enum class TidspunktForAleneomsorg {
    SISTE_2_ÅRENE,
    TIDLIGERE
}