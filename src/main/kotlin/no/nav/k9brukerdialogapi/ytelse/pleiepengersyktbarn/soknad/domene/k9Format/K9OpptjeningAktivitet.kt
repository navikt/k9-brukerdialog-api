package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format

import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Frilans
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.FrilansType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.DAGER_PER_UKE

fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)

internal fun Søknad.byggK9OpptjeningAktivitet(): OpptjeningAktivitet {
    val opptjeningAktivitet = OpptjeningAktivitet()
    if (selvstendigNæringsdrivende.harInntektSomSelvstendig) {
        opptjeningAktivitet.medSelvstendigNæringsdrivende(selvstendigNæringsdrivende.tilK9SelvstendigNæringsdrivende())
    }

    val erFrilanserMedInntenkt = frilans.harInntektSomFrilanser && frilans.type == FrilansType.FRILANS
    if (erFrilanserMedInntenkt) {
        opptjeningAktivitet.medFrilanser(frilans.tilK9Frilanser())
    }
    return opptjeningAktivitet
}

internal fun Frilans.tilK9Frilanser(): Frilanser {
    val frilanser = Frilanser()
    frilanser.medStartdato(startdato)
    sluttdato?.let { frilanser.medSluttdato(it) }
    return frilanser
}
