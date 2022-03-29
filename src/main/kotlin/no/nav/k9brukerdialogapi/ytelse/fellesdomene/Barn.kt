package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    @JsonAlias("identitetsnummer") // Alias frem til omsorgspenger-midlertidig-alene frontend endrer feltnavn.
    private var norskIdentifikator: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val fødselsdato: LocalDate? = null,
    private val aktørId: String? = null,
    private val navn: String
) {

    fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(manglerIdentifikator()) norskIdentifikator = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    fun manglerIdentifikator(): Boolean = norskIdentifikator.isNullOrBlank()

    fun somK9Barn(): K9Barn = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(norskIdentifikator))

    fun valider(): Set<Violation> = mutableSetOf<Violation>().apply {
        if (manglerIdentifikator() || (!norskIdentifikator!!.erGyldigFodselsnummer())) {
            add(
                Violation(
                    parameterName = "barn.norskIdentifikator",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig norskIdentifikator.",
                    invalidValue = norskIdentifikator
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
    }

    override fun toString() = "Barn(aktoerId=${aktørId}, navn=${navn}, fodselsdato=${fødselsdato}"

    override fun equals(other: Any?) = this === other || (other is Barn && this.equals(other))

    private fun equals(other: Barn) =
        this.aktørId == other.aktørId && this.norskIdentifikator == other.norskIdentifikator
}