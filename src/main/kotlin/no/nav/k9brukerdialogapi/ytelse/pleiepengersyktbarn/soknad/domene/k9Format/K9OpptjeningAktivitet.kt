package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format

import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Frilans
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Søknad


internal fun Søknad.byggK9OpptjeningAktivitet(): OpptjeningAktivitet {
    val opptjeningAktivitet = OpptjeningAktivitet()
    if (selvstendigNæringsdrivende.harInntektSomSelvstendig) {
        opptjeningAktivitet.medSelvstendigNæringsdrivende(selvstendigNæringsdrivende.tilK9SelvstendigNæringsdrivende())
    }
    if (frilans.harInntektSomFrilanser) {
        opptjeningAktivitet.medFrilanser(frilans.tilK9Frilanser())
    }
    return opptjeningAktivitet
}

internal fun Frilans.tilK9Frilanser(): Frilanser {
    val frilanser = Frilanser()
    frilanser.medStartdato(startdato)
    return frilanser
}
