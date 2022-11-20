package no.nav.k9brukerdialogapi.ytelse.opplaeringspenger.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.olp.v1.Opplæringspenger
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

private val k9FormatVersjon = Versjon.of("1.1.0")

class Søknad(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    internal val vedlegg: List<URL>,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val bekreftelser: Bekreftelser,
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null
){
    internal fun valider() = mutableListOf<String>().apply {
        addAll(bosteder.valider("bosteder"))
        addAll(opphold.valider("opphold"))
        addAll(bekreftelser.valider("bekreftelser"))

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    internal fun tilKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Søknad,
        titler: List<String> = listOf()
    ) = KomplettSøknad(
        søknadId = søknadId,
        språk = språk,
        mottatt = mottatt,
        søker = søker,
        bosteder = bosteder,
        opphold = opphold,
        bekreftelser = bekreftelser,
        vedleggId = vedlegg.map { it.vedleggId() },
        titler = titler,
        hjemmePgaSmittevernhensyn = hjemmePgaSmittevernhensyn,
        hjemmePgaStengtBhgSkole = hjemmePgaStengtBhgSkole,
        k9Format = k9Format
    )

    internal fun tilK9Format(søker: Søker) = K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.somK9Søker(),
        Opplæringspenger()
    )
}