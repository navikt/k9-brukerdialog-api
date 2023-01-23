package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.fpsak.tidsserie.LocalDateInterval
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.Omsorg
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogapi.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandskNæring.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format.byggK9Arbeidstid
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.k9Format.byggK9OpptjeningAktivitet
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.type.Periode as K9Periode

enum class Språk { nb, nn }

private val k9FormatVersjon = Versjon.of("1.0.0")

data class Søknad(
    val newVersion: Boolean?,
    val apiDataVersjon: String? = null,
    val søknadId: String = UUID.randomUUID().toString(),
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: Språk? = null,
    val barn: BarnDetaljer,
    val arbeidsgivere: List<Arbeidsgiver>,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    val fødselsattestVedleggUrls: List<URL>? = listOf(),
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val nattevåk: Nattevåk? = null,
    val beredskap: Beredskap? = null,
    val frilans: Frilans,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
) : Innsending {

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (barn.manglerIdentitetsnummer()) {
            barn oppdaterFødselsnummer barnFraOppslag.hentIdentitetsnummerForBarn(barn.aktørId)
        }
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): KomplettSøknad {
        requireNotNull(k9Format)
        return KomplettSøknad(
            apiDataVersjon = apiDataVersjon,
            språk = språk,
            søknadId = søknadId,
            mottatt = mottatt,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            søker = søker,
            barn = barn,
            vedleggId = vedlegg.map { it.vedleggId() },
            fødselsattestVedleggId = fødselsattestVedleggUrls?.map { it.vedleggId() } ?: listOf(),
            arbeidsgivere = arbeidsgivere,
            medlemskap = medlemskap,
            ferieuttakIPerioden = ferieuttakIPerioden,
            opptjeningIUtlandet = opptjeningIUtlandet,
            utenlandskNæring = utenlandskNæring,
            utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            omsorgstilbud = omsorgstilbud,
            nattevåk = nattevåk,
            beredskap = beredskap,
            frilans = frilans,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
            barnRelasjon = barnRelasjon,
            barnRelasjonBeskrivelse = barnRelasjonBeskrivelse,
            harVærtEllerErVernepliktig = harVærtEllerErVernepliktig,
            k9FormatSøknad = k9Format as K9Søknad
        )
    }

    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN

    override fun søknadId(): String = søknadId

    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(vedlegg)
        fødselsattestVedleggUrls?.let { addAll(it) }
    }

    override fun valider(): List<String> = mutableListOf<String>().apply {
        addAll(barn.valider("barn"))
        addAll(arbeidsgivere.valider())
        addAll(selvstendigNæringsdrivende.valider())
        addAll(opptjeningIUtlandet.valider())
        addAll(utenlandskNæring.valider("utenlandskNæring"))
        addAll(frilans.valider("frilans"))
        addAll(medlemskap.valider("medlemskap"))
        addAll(utenlandsoppholdIPerioden.valider("utenlandsoppholdIPerioden"))
        krever(harBekreftetOpplysninger, "Opplysningene må bekreftes for å sende inn søknad")
        krever(harForståttRettigheterOgPlikter, "Må ha forstått rettigheter og plikter for å sende inn søknad")

        omsorgstilbud?.let { addAll(it.valider("omsorgstilbud")) }
        ferieuttakIPerioden?.let { addAll(it.valider(("ferieuttakIPerioden"))) }
        beredskap?.let { addAll(it.valider("beredskap")) }
        nattevåk?.let { addAll(it.valider("nattevåk")) }

        addAll(validerBarnRelasjon())

        vedlegg.mapIndexed { index, url ->
            krever(url.path.matches(Regex("/vedlegg/.*")), "vedlegg[$index] inneholder ikke gyldig url")
        }

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))

    }

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = PleiepengerSyktBarnSøknadValidator()

    override fun somK9Format(søker: Søker): no.nav.k9.søknad.Innsending {
        val søknadsperiode = K9Periode(fraOgMed, tilOgMed)
        val psb = PleiepengerSyktBarn()
            .medSøknadsperiode(søknadsperiode)
            .medBarn(barn.tilK9Barn())
            .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
            .medArbeidstid(byggK9Arbeidstid())
            .medUttak(byggK9Uttak(søknadsperiode))
            .medBosteder(medlemskap.tilK9Bosteder())
            .medSøknadInfo(byggK9DataBruktTilUtledning())

        barnRelasjon?.let { psb.medOmsorg(byggK9Omsorg()) }
        beredskap?.let { if (it.beredskap) psb.medBeredskap(beredskap.tilK9Beredskap(søknadsperiode)) }
        nattevåk?.let { if (it.harNattevåk == true) psb.medNattevåk(nattevåk.tilK9Nattevåk(søknadsperiode)) }

        when (omsorgstilbud) {
            null -> psb.medTilsynsordning(tilK9Tilsynsordning0Timer(søknadsperiode))
            else -> psb.medTilsynsordning(omsorgstilbud.tilK9Tilsynsordning(søknadsperiode))
        }

        ferieuttakIPerioden?.let {
            if (it.ferieuttak.isNotEmpty() && it.skalTaUtFerieIPerioden) {
                psb.medLovbestemtFerie(ferieuttakIPerioden.tilK9LovbestemtFerie())
            }
        }

        if (utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true) {
            psb.medUtenlandsopphold(utenlandsoppholdIPerioden.tilK9Utenlandsopphold())
        }

        return K9Søknad(SøknadId.of(søknadId), k9FormatVersjon, mottatt, søker.somK9Søker(), psb)
    }

    fun byggK9Uttak(periode: K9Periode): Uttak {
        val perioder = mutableMapOf<K9Periode, Uttak.UttakPeriodeInfo>()

        perioder[periode] = Uttak.UttakPeriodeInfo(Duration.ofHours(7).plusMinutes(30))

        return Uttak().medPerioder(perioder)
    }

    fun byggK9DataBruktTilUtledning(): DataBruktTilUtledning = DataBruktTilUtledning(
        harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger,
        null,
        null,
        null
    )

    fun byggK9Omsorg() = Omsorg()
        .medRelasjonTilBarnet(
            when (barnRelasjon) {
                BarnRelasjon.FAR -> Omsorg.BarnRelasjon.FAR
                BarnRelasjon.MOR -> Omsorg.BarnRelasjon.MOR
                BarnRelasjon.FOSTERFORELDER -> Omsorg.BarnRelasjon.FOSTERFORELDER
                BarnRelasjon.MEDMOR -> Omsorg.BarnRelasjon.MEDMOR
                BarnRelasjon.ANNET -> Omsorg.BarnRelasjon.ANNET
                else -> null
            }
        ).medBeskrivelseAvOmsorgsrollen(barnRelasjonBeskrivelse)
}

fun tilK9Tilsynsordning0Timer(periode: no.nav.k9.søknad.felles.type.Periode) = Tilsynsordning().apply {
    leggeTilPeriode(
        periode,
        TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
            Duration.ZERO
        )
    )
}

private fun Søknad.validerBarnRelasjon() = mutableListOf<String>().apply {
    if (barnRelasjon == BarnRelasjon.ANNET) {
        krever(
            !barnRelasjonBeskrivelse.isNullOrBlank(),
            "Når barnRelasjon er ANNET, kan ikke barnRelasjonBeskrivelse være tom"
        )
    }
}

private fun List<BarnOppslag>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return this.firstOrNull() { it.aktørId == aktørId }?.identitetsnummer
}

data class Periode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
) {
    fun somLocalDateInterval() = LocalDateInterval(fraOgMed, tilOgMed)
}
