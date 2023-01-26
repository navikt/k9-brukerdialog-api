package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.validerIdentifikator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

private val logger: Logger =
    LoggerFactory.getLogger("no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.BarnDetaljer")

data class BarnDetaljer(
    var fødselsnummer: String?,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate?,
    val aktørId: String?,
    val navn: String?,
    val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=***, navn=***, fodselsdato=***"
    }

    fun manglerIdentitetsnummer(): Boolean = fødselsnummer.isNullOrEmpty()

    infix fun oppdaterFødselsnummer(fødselsnummer: String?) {
        logger.info("Forsøker å oppdaterer fnr på barn")
        this.fødselsnummer = fødselsnummer
    }

    fun tilK9Barn(): Barn = when {
        fødselsnummer != null -> K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(fødselsnummer))
        fødselsdato != null -> K9Barn().medFødselsdato(fødselsdato)
        else -> K9Barn()
    }
}

enum class ÅrsakManglerIdentitetsnummer {
    NYFØDT,
    BARNET_BOR_I_UTLANDET,
    ANNET
}

internal fun BarnDetaljer.valider(felt: String) = mutableListOf<String>().apply {
    navn?.let { krever(it.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.") }
    fødselsdato?.let { krever(it.erFørEllerLik(LocalDate.now()), "$felt.fødselsdato kan ikke være i fremtiden.") }
    if (fødselsnummer.isNullOrEmpty()) {
        krever(fødselsdato != null, "$felt.fødselsdato må være satt dersom fødselsnummer er null.")
        krever(
            årsakManglerIdentitetsnummer != null,
            "$felt.årsakManglerIdentitetsnummer må være satt dersom fødselsnummer er null."
        )
    } else {
        validerIdentifikator(fødselsnummer, "$felt.fødselsnummer")
    }
}
