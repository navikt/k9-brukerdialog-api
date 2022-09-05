package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Bosteder.BostedPeriodeInfo
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.krever

class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean? = null,
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean? = null,
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
) {
    internal fun valider(felt: String = "medlemskap") = mutableListOf<String>().apply {
        krever(harBoddIUtlandetSiste12Mnd != null, "$felt.harBoddIUtlandetSiste12Mnd kan ikke være null.")
        krever(skalBoIUtlandetNeste12Mnd != null, "$felt.skalBoIUtlandetNeste12Mnd kan ikke være null.")
        utenlandsoppholdSiste12Mnd.forEachIndexed { index, utenlandsopphold ->
            addAll(utenlandsopphold.valider("$felt.utenlandsoppholdSiste12Mnd[$index]"))
        }
        utenlandsoppholdNeste12Mnd.forEachIndexed { index, utenlandsopphold ->
            addAll(utenlandsopphold.valider("$felt.utenlandsoppholdNeste12Mnd[$index]"))
        }
    }

    internal fun somK9Bosteder() = Bosteder().medPerioder(
        mutableMapOf<Periode, BostedPeriodeInfo>().apply {
            utenlandsoppholdSiste12Mnd.forEach { this[it.k9Periode()] = it.somK9BostedPeriodeInfo() }
            utenlandsoppholdNeste12Mnd.forEach { this[it.k9Periode()] = it.somK9BostedPeriodeInfo() }
        }
    )
}