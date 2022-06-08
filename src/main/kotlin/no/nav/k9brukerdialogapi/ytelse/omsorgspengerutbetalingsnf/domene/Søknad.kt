package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsperiode
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class Søknad(
    internal val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val spørsmål: List<SpørsmålOgSvar>,
    private val bekreftelser: Bekreftelser,
    private val utbetalingsperioder: List<Utbetalingsperiode>
) {
}