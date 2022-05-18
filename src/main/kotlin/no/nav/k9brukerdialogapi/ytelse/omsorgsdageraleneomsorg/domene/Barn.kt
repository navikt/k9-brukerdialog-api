package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.TidspunktForAleneomsorg.SISTE_2_ÅRENE
import no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.TidspunktForAleneomsorg.TIDLIGERE
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
    internal fun somK9Barn() =  K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(manglerIdentifikator()) identitetsnummer = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun k9PeriodeFraOgMed() = when (tidspunktForAleneomsorg) {
        SISTE_2_ÅRENE -> dato
        TIDLIGERE -> LocalDate.now().minusYears(1).startenAvÅret()
    }

    private fun LocalDate.startenAvÅret() = LocalDate.parse("${year}-01-01")

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt/blankt.")
        krever(navn.length <= 100, "$felt.navn kan ikke være over 100 tegn.")
        krever(!manglerIdentifikator(), "$felt.identitetsnummer må være satt.")
        identitetsnummer?.let { krever(it.erGyldigFodselsnummer(), "$felt.identitetsnummer må være gyldig.")}
        if(type != TypeBarn.FRA_OPPSLAG) krever(fødselsdato != null, "$felt.fødselsdato må være satt når type!=FRA_OPPSLAG.")
        if(tidspunktForAleneomsorg == SISTE_2_ÅRENE) krever(dato != null, "$felt.dato må være satt.")
    }

    override fun equals(other: Any?) = this === other || other is Barn && this.equals(other)
    private fun equals(other: Barn) = this.identitetsnummer == other.identitetsnummer
}

enum class TidspunktForAleneomsorg {
    SISTE_2_ÅRENE,
    TIDLIGERE
}