package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetalingSøknadValidator
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.somK9Bosteder
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.somK9Utenlandsopphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Arbeidsgiver.Companion.somK9Fraværsperiode
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Arbeidsgiver.Companion.valider
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

private val k9FormatVersjon = Versjon.of("1.1.0")

class OmsorgspengerutbetalingArbeidstakerSøknad(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    internal val vedlegg: List<URL>,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val bekreftelser: Bekreftelser,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null
): Innsending {
    override fun valider() = mutableListOf<String>().apply {
        krever(arbeidsgivere.isNotEmpty(), "Må ha minst en arbeidsgiver satt.")
        addAll(bosteder.valider("bosteder"))
        addAll(opphold.valider("opphold"))
        addAll(bekreftelser.valider("bekreftelser"))
        addAll(arbeidsgivere.valider("arbeidsgivere"))

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>
    ): OmsorgspengerutbetalingArbeidstakerKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerutbetalingArbeidstakerKomplettSøknad(
            søknadId = søknadId,
            språk = språk,
            mottatt = mottatt,
            søker = søker,
            bosteder = bosteder,
            opphold = opphold,
            arbeidsgivere = arbeidsgivere,
            bekreftelser = bekreftelser,
            vedleggId = vedlegg.map { it.vedleggId() },
            titler = titler,
            hjemmePgaSmittevernhensyn = hjemmePgaSmittevernhensyn,
            hjemmePgaStengtBhgSkole = hjemmePgaStengtBhgSkole,
            k9Format = k9Format as no.nav.k9.søknad.Søknad
        )
    }

    override fun somK9Format(søker: Søker): no.nav.k9.søknad.Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            OmsorgspengerUtbetaling(
                null,
                OpptjeningAktivitet(),
                arbeidsgivere.somK9Fraværsperiode(),
                null,
                bosteder.somK9Bosteder(),
                opphold.somK9Utenlandsopphold()
            )
        )
    }

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerUtbetalingSøknadValidator()
    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = vedlegg
}
