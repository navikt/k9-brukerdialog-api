package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import com.fasterxml.jackson.annotation.JsonAlias

enum class Arbeidssituasjon {
    @JsonAlias("annen") ANNEN,
    @JsonAlias("frilanser") FRILANSER,
    @JsonAlias("arbeidstaker") ARBEIDSTAKER,
    @JsonAlias("selvstendigNæringsdrivende") SELVSTENDIG_NÆRINGSDRIVENDE
}