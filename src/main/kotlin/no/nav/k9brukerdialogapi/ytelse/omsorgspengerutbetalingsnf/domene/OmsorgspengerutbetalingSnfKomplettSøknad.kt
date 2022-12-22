package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogapi.innsending.KomplettInnsending
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class OmsorgspengerutbetalingSnfKomplettSøknad(
    internal val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val søker: Søker,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val spørsmål: List<SpørsmålOgSvar>,
    private val harDekketTiFørsteDagerSelv: Boolean? = null,
    private val bekreftelser: Bekreftelser,
    private val utbetalingsperioder: List<Utbetalingsperiode>,
    @Deprecated("Kan fjernes etter lansering") private val andreUtbetalinger: List<AndreUtbetalinger>? = null,
    private val erArbeidstakerOgså: Boolean,
    private val barn: List<Barn>,
    private val frilans: Frilans? = null,
    private val selvstendigNæringsdrivende: Virksomhet? = null,
    private val vedleggId: List<String> = listOf(),
    private val titler: List<String>,
    private val k9FormatSøknad: Søknad
): KomplettInnsending {
    override fun equals(other: Any?) = this === other || (other is OmsorgspengerutbetalingSnfKomplettSøknad && this.equals(other))

    private fun equals(other: OmsorgspengerutbetalingSnfKomplettSøknad) = this.søknadId.id == other.søknadId.id &&
            this.søker == other.søker &&
            this.vedleggId == other.vedleggId &&
            this.k9FormatSøknad.søknadId == other.k9FormatSøknad.søknadId
}
