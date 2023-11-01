package no.nav.k9brukerdialogapi.ytelse

import io.ktor.server.application.ApplicationCall

enum class Ytelse(val dialog: String) {
    OMSORGSPENGER_UTVIDET_RETT("omsorgspengesoknad"),
    OMSORGSPENGER_MIDLERTIDIG_ALENE("ekstra-omsorgsdager-andre-forelder-ikke-tilsyn"),
    ETTERSENDING("sif-ettersending"),
    OMSORGSDAGER_ALENEOMSORG("omsorgsdager-aleneomsorg-dialog"),
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER("omsorgspengerutbetaling-arbeidstaker-soknad"),
    OMSORGSPENGER_UTBETALING_SNF("omsorgspengerutbetaling-soknad"),
    PLEIEPENGER_LIVETS_SLUTTFASE("pleiepenger-i-livets-sluttfase-soknad"),
    ETTERSENDING_PLEIEPENGER_SYKT_BARN("sif-ettersending"),
    ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE("sif-ettersending"),
    ETTERSENDING_OMP("sif-ettersending"),
    PLEIEPENGER_SYKT_BARN("pleiepengesoknad"),
    ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN("endringsmelding-pleiepenger");
}

fun ApplicationCall.ytelseFraHeader(): Ytelse {
    val k9BrukerdialogHeader =
        request.headers["X-K9-Brukerdialog"] ?: throw IllegalArgumentException("Mangler header X-K9-Brukerdialog")

    return runCatching { Ytelse.entries.first { it.dialog == k9BrukerdialogHeader.substringAfterLast(":") } }
        .onSuccess { return it }
        .onFailure { throw IllegalArgumentException("Ukjent dialog $k9BrukerdialogHeader") }
        .getOrThrow()
}
