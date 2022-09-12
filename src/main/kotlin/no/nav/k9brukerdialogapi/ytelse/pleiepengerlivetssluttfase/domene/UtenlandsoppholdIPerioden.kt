package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Utenlandsopphold.Companion.valider
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold

class UtenlandsoppholdIPerioden(
    internal val skalOppholdeSegIUtlandetIPerioden: Boolean? = null,
    private val opphold: List<Utenlandsopphold> = listOf()
) {
    internal fun valider(felt: String = "utenlandsoppholdIPerioden") = mutableListOf<String>().apply {
        addAll(opphold.valider("$felt.opphold"))
    }

    internal fun somK9Utenlandsopphold() = K9Utenlandsopphold().medPerioder(
        mutableMapOf<Periode, UtenlandsoppholdPeriodeInfo>().apply {
            opphold.forEach { this[it.k9Periode()] = it.somK9UtenlandsoppholdPeriodeInfo() }
        }
    )
}