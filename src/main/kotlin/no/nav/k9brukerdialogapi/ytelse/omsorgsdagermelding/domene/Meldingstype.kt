package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import com.fasterxml.jackson.annotation.JsonAlias

enum class Meldingstype {
    @JsonAlias("fordeling") FORDELING,
    @JsonAlias("overføring") OVERFORING,
    @JsonAlias("koronaoverføring") KORONA,
}
