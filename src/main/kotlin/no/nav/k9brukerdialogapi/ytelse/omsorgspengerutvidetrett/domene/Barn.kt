package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    private var norskIdentifikator: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val fødselsdato: LocalDate? = null,
    private val aktørId: String? = null,
    private val navn: String
) {
    companion object{
        fun leggTilIdentifikatorPåBarnSomMangler(barn: Barn, barnFraOppslag: List<BarnOppslag>){
            if(barn.manglerIdentifikator()) barn.norskIdentifikator = barnFraOppslag.find { it.aktørId == barn.aktørId }?.identitetsnummer
        }
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
}