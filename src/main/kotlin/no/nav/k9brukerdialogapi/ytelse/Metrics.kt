package no.nav.k9brukerdialogapi.ytelse

import io.prometheus.client.Counter

val mottatteSøknaderPerYtelseCounter = Counter.build()
    .help("Teller antall mottatte søknader per ytelse")
    .name("mottatte_soknader_per_ytelse_counter")
    .labelNames("ytelse")
    .register()

fun registrerMottattSøknad(ytelse: Ytelse) = mottatteSøknaderPerYtelseCounter.labels(ytelse.name).inc()