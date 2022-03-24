package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad

private val k9FormatVersjon = Versjon.of("1.0.0")

fun Søknad.tilK9Format(søker: Søker): no.nav.k9.søknad.Søknad {
    return no.nav.k9.søknad.Søknad(
        SøknadId.of(this.søknadId),
        k9FormatVersjon,
        mottatt,
        søker.somK9Søker(),
        OmsorgspengerKroniskSyktBarn(
            barn.somK9Barn(),
            kroniskEllerFunksjonshemming
        )
    )
}

