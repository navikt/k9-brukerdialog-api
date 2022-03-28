package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder as K9AnnenForelder

class AnnenForelder(
    private val navn: String,
    private val fnr: String,
    private val situasjon: Situasjon,
    private val situasjonBeskrivelse: String? = null,
    private val periodeOver6Måneder: Boolean? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val periodeFraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val periodeTilOgMed: LocalDate? = null
) {

    internal fun somK9AnnenForelder(): K9AnnenForelder {
        return K9AnnenForelder()
            .medNorskIdentitetsnummer(NorskIdentitetsnummer.of(fnr))
            .medSituasjon(situasjon.somK9SituasjonType(), situasjonBeskrivelse)
            .apply {
                if(periodeTilOgMed != null) this.medPeriode(Periode(periodeFraOgMed, periodeTilOgMed))
            }
    }

    override fun equals(other: Any?) = this === other || (other is AnnenForelder && this.equals(other))

    private fun equals(other: AnnenForelder) = this.fnr == other.fnr && this.navn == other.navn

    internal fun valider() = mutableSetOf<Violation>().apply {
        if (navn.isNullOrBlank()) {
            add(
                Violation(
                    parameterName = "AnnenForelder.navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navn på annen forelder kan ikke være null, tom eller kun white spaces",
                    invalidValue = navn
                )
            )
        }

        if(!fnr.erGyldigFodselsnummer()){
            add(
                Violation(
                    parameterName = "AnnenForelder.fnr",
                    parameterType = ParameterType.ENTITY,
                    reason = "Fødselsnummer på annen forelder må være gyldig norsk identifikator",
                    invalidValue = fnr
                )
            )
        }

        if (periodeTilOgMed != null && periodeFraOgMed.isAfter(periodeTilOgMed)) {
            add(
                Violation(
                    parameterName = "AnnenForelder.periodeFraOgMed",
                    parameterType = ParameterType.ENTITY,
                    reason = "periodeFraOgMed kan ikke være etter periodeTilOgMed",
                    invalidValue = periodeFraOgMed
                )
            )
        }

        when(situasjon){
            Situasjon.INNLAGT_I_HELSEINSTITUSJON -> addAll(validerBekreftelsePeriodeOver6Mnd(situasjon))
            Situasjon.UTØVER_VERNEPLIKT, Situasjon.FENGSEL -> addAll(validerVærnepliktEllerFengsel(situasjon))
            Situasjon.SYKDOM, Situasjon.ANNET -> {
                addAll(validerSituasjonBeskrivelse(situasjon))
                addAll(validerBekreftelsePeriodeOver6Mnd(situasjon))
            }
        }
    }

    private fun validerBekreftelsePeriodeOver6Mnd(situasjon: Situasjon) = mutableSetOf<Violation>().apply {
        if (periodeTilOgMed == null && periodeOver6Måneder == null) {
            add(
                Violation(
                    parameterName = "AnnenForelder.periodeOver6Måneder",
                    parameterType = ParameterType.ENTITY,
                    reason = "periodeOver6Måneder kan ikke være null når periodeTilOgMed er null, og situasjonen er $situasjon",
                    invalidValue = periodeFraOgMed
                )
            )
        }
    }

    private fun validerVærnepliktEllerFengsel(situasjon: Situasjon) = mutableSetOf<Violation>().apply {
        if (periodeTilOgMed == null) {
            add(
                Violation(
                    parameterName = "AnnenForelder.periodeTilOgMed",
                    parameterType = ParameterType.ENTITY,
                    reason = "periodeTilOgMed kan ikke være null dersom situasjonen er $situasjon",
                    invalidValue = periodeTilOgMed
                )
            )
        }
    }

    private fun validerSituasjonBeskrivelse(situasjon: Situasjon) = mutableSetOf<Violation>().apply {
        if (situasjonBeskrivelse.isNullOrBlank()) {
            add(
                Violation(
                    parameterName = "AnnenForelder.situasjonBeskrivelse",
                    parameterType = ParameterType.ENTITY,
                    reason = "Situasjonsbeskrivelse på annenForelder kan ikke være null, tom eller kun white spaces når situasjon er $situasjon",
                    invalidValue = situasjonBeskrivelse
                )
            )
        }

        if (!situasjonBeskrivelse.isNullOrBlank() && (situasjonBeskrivelse.length !in 5..1000)) {
            add(
                Violation(
                    parameterName = "AnnenForelder.situasjonBeskrivelse",
                    parameterType = ParameterType.ENTITY,
                    reason = "Situasjonsbeskrivelse på annenForelder kan kun ha en lengde mellom 5 til 1000 tegn.",
                    invalidValue = situasjonBeskrivelse.length
                )
            )
        }
    }
}