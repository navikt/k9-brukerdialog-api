package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

enum class TypeBarn{
    FRA_OPPSLAG,
    FOSTERBARN,
    ANNET
}

class Barn(
    private val navn: String,
    private val type: TypeBarn,
    private val aktørId: String? = null,
    private var identitetsnummer: String? = null,
    private val tidspunktForAleneomsorg: TidspunktForAleneomsorg,
    private val dato: LocalDate? = null,
    private val fødselsdato: LocalDate? = null
) {
    internal fun manglerIdentifikator() = identitetsnummer.isNullOrBlank()

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(manglerIdentifikator()) identitetsnummer = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun somK9Barn() =  K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))

    internal fun k9PeriodeFraOgMed() = when (tidspunktForAleneomsorg) {
        TidspunktForAleneomsorg.SISTE_2_ÅRENE -> dato
        TidspunktForAleneomsorg.TIDLIGERE -> LocalDate.now().minusYears(1).startenAvÅret()
    }

    private fun LocalDate.startenAvÅret() = LocalDate.parse("${year}-01-01")

    internal fun valider(): Set<Violation> = mutableSetOf<Violation>().apply {
        if (manglerIdentifikator() || (!identitetsnummer!!.erGyldigFodselsnummer())) {
            add(
                Violation(
                    parameterName = "barn.identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig identitetsnummer."
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

        if(type != TypeBarn.FRA_OPPSLAG && fødselsdato == null){
            add(
                Violation(
                    parameterName = "barn.fødselsdato",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn som ikke stammer fra oppslag må ha fødselsdato satt.",
                    invalidValue = fødselsdato
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