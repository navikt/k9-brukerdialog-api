package no.nav.k9brukerdialogapi.oppslag.søker

import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

private const val MYNDIG_ALDER = 18L

class Søker (
    private val aktørId: String,
    private val fødselsdato: LocalDate,
    private val fødselsnummer: String,
    private val fornavn: String? = null,
    private val mellomnavn: String? = null,
    private val etternavn: String? = null
) {
    /*
    init {
        require(erMyndig()) { "Søker må være myndig for å søke." } //Kan erstatte valideringsjekk
    }
    */

    fun erMyndig(): Boolean {
        val attenÅrSiden = LocalDate.now().minusYears(MYNDIG_ALDER)
        return fødselsdato.isBefore(attenÅrSiden) || fødselsdato.isEqual(attenÅrSiden)
    }

    fun somK9Søker() = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

    fun somDokumentEier() = DokumentEier(fødselsnummer)

    fun valider() {
        if (!erMyndig()) {
            throw Throwblem(
                DefaultProblemDetails(
                    title = "unauthorized",
                    status = 403,
                    detail = "Søkeren er ikke myndig og kan ikke sende inn søknaden."
                )
            )
        }
    }
}

data class SøkerOppslagRespons(
    val aktør_id: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate
) {
    fun tilSøker(fødselsnummer: String) = Søker(
        aktørId = aktør_id,
        fødselsnummer = fødselsnummer,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}