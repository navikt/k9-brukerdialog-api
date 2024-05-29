package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.innsending.KomplettInnsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import java.time.ZonedDateTime

class OmsorgspengerutbetalingArbeidstakerKomplettSøknad(
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
    private val fosterbarn: List<Fosterbarn>? = null, // TODO: Fjern nullable når lansert
    private val dineBarn: DineBarn? = null, // TODO: Fjern nullable når lansert
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null,
    private val k9Format: Søknad
): KomplettInnsending {
    override fun equals(other: Any?) = this === other || (other is OmsorgspengerutbetalingArbeidstakerKomplettSøknad && this.equals(other))

    private fun equals(other: OmsorgspengerutbetalingArbeidstakerKomplettSøknad) = this.søknadId == other.søknadId &&
            this.søker == other.søker &&
            this.vedleggId == other.vedleggId &&
            this.k9Format.søknadId == other.k9Format.søknadId
}
