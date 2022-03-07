package no.nav.k9brukerdialogapi

import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.omsorgspenger.utvidetrett.domene.Barn

val gyldigSøknad = Søknad(
    språk = "nb",
    kroniskEllerFunksjonshemming = true,
    barn = Barn(
        norskIdentifikator = null,
        fødselsdato = null,
        aktørId = "1000000000001",
        navn = "Barn Barnesen"
    ),
    relasjonTilBarnet = SøkerBarnRelasjon.FAR,
    sammeAdresse = true,
    legeerklæring = listOf(),
    samværsavtale = listOf(),
    harBekreftetOpplysninger = true,
    harForståttRettigheterOgPlikter = true
)