package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.somK9Bosteder
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.somK9Utenlandsopphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode.Companion.somK9FraværPeriode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn.Companion.somK9BarnListe
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn.Companion.valider
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class Søknad(
    internal val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val spørsmål: List<SpørsmålOgSvar>,
    private val harDekketTiFørsteDagerSelv: Boolean? = null,
    private val bekreftelser: Bekreftelser,
    private val utbetalingsperioder: List<Utbetalingsperiode>,
    private val andreUtbetalinger: List<AndreUtbetalinger>,
    private val erArbeidstakerOgså: Boolean,
    private val barn: List<Barn>,
    private val frilans: Frilans? = null,
    private val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    internal val vedlegg: List<URL> = listOf()
) {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    internal fun valider() = mutableListOf<String>().apply {
        addAll(validerUtvidetRett())
        addAll(validerHarDekketTiFørsteDagerSelv())
        addAll(bosteder.valider("bosteder"))
        addAll(opphold.valider("opphold"))
        addAll(bekreftelser.valider("bekreftelser"))
        addAll(utbetalingsperioder.valider("utbetalingsperioder"))
        addAll(barn.valider("barn"))
        frilans?.let { addAll(it.valider("frilans")) }
        selvstendigNæringsdrivende?.let { addAll(it.valider("selvstendigNæringsdrivende")) }

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    private fun validerUtvidetRett() = mutableListOf<String>().apply {
        if(barn.all { it.trettenÅrEllerEldre() }){
            krever(barn.any{ it.utvidetRett == true}, "Hvis alle barna er 13 år eller eldre må minst et barn ha utvidet rett.")
        }
    }

    private fun validerHarDekketTiFørsteDagerSelv() = mutableListOf<String>().apply {
        if(barn.any { it.tolvÅrEllerYngre() }){
            krever(harDekketTiFørsteDagerSelv, "Dersom et barn er 12 år eller yngre må harDekketTiFørsteDagerSelv være true.")
        }
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    internal fun somK9Format(søker: Søker) = K9Søknad(
        søknadId,
        k9FormatVersjon,
        mottatt,
        søker.somK9Søker(),
        OmsorgspengerUtbetaling(
            barn.somK9BarnListe(),
            byggK9OpptjeningAktivitet(),
            utbetalingsperioder.somK9FraværPeriode(),
            null,
            bosteder.somK9Bosteder(),
            opphold.somK9Utenlandsopphold()
        )
    )

    private fun byggK9OpptjeningAktivitet() = OpptjeningAktivitet().apply {
        frilans?.let { medFrilanser(it.somK9Frilanser()) }
        this@Søknad.selvstendigNæringsdrivende?.let { medSelvstendigNæringsdrivende(it.somK9SelvstendigNæringsdrivende()) }
    }

    internal fun tilKomplettSøknad(søker: Søker, k9Format: no.nav.k9.søknad.Søknad) = KomplettSøknad(
        søknadId = søknadId,
        mottatt = mottatt,
        språk = språk,
        søker = søker,
        bosteder = bosteder,
        opphold = opphold,
        spørsmål = spørsmål,
        harDekketTiFørsteDagerSelv = harDekketTiFørsteDagerSelv,
        bekreftelser = bekreftelser,
        utbetalingsperioder = utbetalingsperioder,
        andreUtbetalinger = andreUtbetalinger,
        erArbeidstakerOgså = erArbeidstakerOgså,
        barn = barn,
        frilans = frilans,
        selvstendigNæringsdrivende = selvstendigNæringsdrivende,
        vedleggId = vedlegg.map { it.vedleggId() },
        k9FormatSøknad = k9Format
    )
}