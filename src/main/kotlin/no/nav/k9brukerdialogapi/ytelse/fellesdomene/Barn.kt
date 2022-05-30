package no.nav.k9brukerdialogapi.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
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

    fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>){
        if(manglerIdentifikator()) norskIdentifikator = barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    fun manglerIdentifikator(): Boolean = norskIdentifikator.isNullOrBlank()

    fun somK9Barn(): K9Barn = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(norskIdentifikator))

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        validerIdentifikator(norskIdentifikator, "$felt.norskIdentifikator")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blank.")
    }

    override fun toString() = "Barn(aktoerId=${aktørId}, navn=${navn}, fodselsdato=${fødselsdato}"

    override fun equals(other: Any?) = this === other || (other is Barn && this.equals(other))
    private fun equals(other: Barn) = this.aktørId == other.aktørId && this.norskIdentifikator == other.norskIdentifikator
}