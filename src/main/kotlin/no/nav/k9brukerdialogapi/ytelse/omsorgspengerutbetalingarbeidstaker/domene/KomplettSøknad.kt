package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import java.time.ZonedDateTime

class KomplettSøknad(
    internal val søknadId: String,
    private val mottatt: ZonedDateTime,
    private val språk: String,
    private val søker: Søker,
    private val vedleggId: List<String>,
    private val titler: List<String>,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val bekreftelser: Bekreftelser,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null,
    private val k9Format: Søknad
) {
    override fun equals(other: Any?) = this === other || (other is KomplettSøknad && this.equals(other))

    private fun equals(other: KomplettSøknad) = this.søknadId == other.søknadId &&
            this.søker == other.søker &&
            this.vedleggId == other.vedleggId &&
            this.k9Format.søknadId == other.k9Format.søknadId
}