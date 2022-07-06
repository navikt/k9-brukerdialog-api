package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import com.fasterxml.jackson.annotation.JsonAlias

enum class Mottaker() {
    @JsonAlias("samboer") SAMBOER,
    @JsonAlias("ektefelle") EKTEFELLE,
    @JsonAlias("samværsforelder") SAMVÆRSFORELDER
}