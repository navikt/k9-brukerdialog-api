package no.nav.k9brukerdialogapi.ytelse

import io.prometheus.client.Counter

val mottatteSøknaderPerYtelseCounter = Counter.build()
    .help("Teller antall mottatte søknader per ytelse")
    .name("mottatteSøknaderPerYtelseCounter")
    .labelNames("ytelse")
    .register()

fun registrerMottattSøknad(ytelse: Ytelse) = mottatteSøknaderPerYtelseCounter.labels("ytelse", ytelse.name).inc()