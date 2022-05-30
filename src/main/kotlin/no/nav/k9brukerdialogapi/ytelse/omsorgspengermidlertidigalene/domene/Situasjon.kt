package no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder

enum class Situasjon {
    @JsonAlias("innlagtIHelseinstitusjon")
    INNLAGT_I_HELSEINSTITUSJON,

    @JsonAlias("utøverVerneplikt")
    UTØVER_VERNEPLIKT,

    @JsonAlias("fengsel")
    FENGSEL,

    @JsonAlias("sykdom")
    SYKDOM,

    @JsonAlias("annet")
    ANNET;

    internal fun somK9SituasjonType() = when (this) {
        INNLAGT_I_HELSEINSTITUSJON -> AnnenForelder.SituasjonType.INNLAGT_I_HELSEINSTITUSJON
        UTØVER_VERNEPLIKT -> AnnenForelder.SituasjonType.UTØVER_VERNEPLIKT
        FENGSEL -> AnnenForelder.SituasjonType.FENGSEL
        SYKDOM -> AnnenForelder.SituasjonType.SYKDOM
        ANNET -> AnnenForelder.SituasjonType.ANNET
    }
}