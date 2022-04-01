package no.nav.k9brukerdialogapi.ytelse

import io.prometheus.client.Counter

val antallMottatteSøknaderPerYtelseCounter = Counter.build()
    .help("Teller antall mottatte søknader per ytelse")
    .name("antall_mottatte_soknader_per_ytelse_counter")
    .labelNames("ytelse")
    .register()

fun registrerMottattSøknad(ytelse: Ytelse) = antallMottatteSøknaderPerYtelseCounter.labels(ytelse.name).inc()