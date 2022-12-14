package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import java.time.Duration

internal val NormalArbeidsdag = Duration.ofHours(7).plusMinutes(30)

fun Duration.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(duration: Duration?): Duration {
    return when {
        duration == null -> this
        duration > NormalArbeidsdag -> plus(NormalArbeidsdag)
        else -> plus(duration)
    }
}
