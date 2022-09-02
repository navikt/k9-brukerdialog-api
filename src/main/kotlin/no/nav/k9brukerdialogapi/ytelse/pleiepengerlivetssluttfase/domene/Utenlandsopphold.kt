package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.personopplysninger.Bosteder.BostedPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Land
import java.time.LocalDate

class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val tilOgMed: LocalDate,
    private val landkode: String,
    private val landnavn: String // TODO: 02/09/2022 Refaktorere til å bruke klassen land i stedet. Må endre frontend
) {
    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(Land(landkode = landkode, landnavn = landnavn).valider("$felt.landkode/landnavn"))
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før eller lik tilOgMed.")
    }

    internal fun k9Periode() = Periode(fraOgMed, tilOgMed)
    internal fun somK9BostedPeriodeInfo() = BostedPeriodeInfo().medLand(Landkode.of(landkode))
}