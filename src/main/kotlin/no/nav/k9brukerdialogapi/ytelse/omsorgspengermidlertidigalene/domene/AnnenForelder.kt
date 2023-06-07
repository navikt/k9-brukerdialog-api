package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import no.nav.k9brukerdialogapi.utils.StringUtils
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.ANNET
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.FENGSEL
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.INNLAGT_I_HELSEINSTITUSJON
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.SYKDOM
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.UTØVER_VERNEPLIKT
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
            .medSituasjon(situasjon.somK9SituasjonType(), situasjonBeskrivelse?.let { StringUtils.saniter(it) })
            .apply {
                if(periodeTilOgMed != null) this.medPeriode(Periode(periodeFraOgMed, periodeTilOgMed))
            }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(fnr, "$felt.fnr")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blank.")
        periodeTilOgMed?.let { krever(periodeTilOgMed.erLikEllerEtter(periodeFraOgMed), "$felt.periodeTilOgMed må være lik eller etter periodeFraOgMed.") }

        when(situasjon){
            INNLAGT_I_HELSEINSTITUSJON -> validerGyldigPeriodeSatt(felt, situasjon)
            UTØVER_VERNEPLIKT, FENGSEL -> validerVærnepliktEllerFengsel(felt)
            SYKDOM, ANNET -> {
                validerGyldigPeriodeSatt(felt, situasjon)
                krever(!situasjonBeskrivelse.isNullOrBlank(), "$felt.situasjonBeskrivelse kan ikke være null eller tom dersom situasjon er $situasjon")
            }
        }
    }

    private fun MutableList<String>.validerVærnepliktEllerFengsel(felt: String) =
        kreverIkkeNull(periodeTilOgMed, "$felt.periodeTilOgMed kan ikke være null dersom situasjonen er $FENGSEL eller $UTØVER_VERNEPLIKT")

    private fun MutableList<String>.validerGyldigPeriodeSatt(felt: String, situasjon: Situasjon) {
        krever(
            periodeTilOgMed != null || periodeOver6Måneder != null,
            "$felt.periodeTilOgMed eller periodeOver6Måneder må være satt dersom situasjonen er $situasjon"
        )
    }

    override fun equals(other: Any?) = this === other || (other is AnnenForelder && this.equals(other))
    private fun equals(other: AnnenForelder) = this.fnr == other.fnr && this.navn == other.navn
}
