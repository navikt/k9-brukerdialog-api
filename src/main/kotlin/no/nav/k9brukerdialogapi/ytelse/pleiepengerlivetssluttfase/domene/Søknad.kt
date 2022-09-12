package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.pls.v1.PleipengerLivetsSluttfase
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.Uttak.UttakPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.vedlegg.vedleggId
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.SYV_OG_EN_HALV_TIME
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.ArbeidUtils.arbeidstidInfoMedNullTimer
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsgiver.Companion.somK9Arbeidstaker
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsgiver.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.OpptjeningIUtlandet.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.UtenlandskNæring.Companion.valider
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class Søknad(
    private val søknadId: String = UUID.randomUUID().toString(),
    private val språk: String,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate,
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val vedleggUrls: List<URL> = listOf(),
    private val opplastetIdVedleggUrls: List<URL> = listOf(),
    private val pleietrengende: Pleietrengende,
    private val medlemskap: Medlemskap,
    private val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val frilans: Frilans? = null,
    private val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    private val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    private val utenlandskNæring: List<UtenlandskNæring>,
    private val harVærtEllerErVernepliktig: Boolean? = null,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
) {
    companion object{
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
    }

    internal fun somKomplettSøknad(søker: Søker, k9Format: K9Søknad) = KomplettSøknad(
        søknadId = søknadId,
        søker = søker,
        språk = språk,
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
        mottatt = mottatt,
        vedleggId = vedleggUrls.map { it.vedleggId() },
        opplastetIdVedleggId = opplastetIdVedleggUrls.map { it.vedleggId() },
        medlemskap = medlemskap,
        pleietrengende = pleietrengende,
        utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
        frilans = frilans,
        arbeidsgivere = arbeidsgivere,
        opptjeningIUtlandet = opptjeningIUtlandet,
        utenlandskNæring = utenlandskNæring,
        selvstendigNæringsdrivende = selvstendigNæringsdrivende,
        harVærtEllerErVernepliktig = harVærtEllerErVernepliktig,
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = harBekreftetOpplysninger,
        k9Format = k9Format
    )

    internal fun valider() = mutableListOf<String>().apply {
        addAll(medlemskap.valider())
        addAll(arbeidsgivere.valider())
        addAll(pleietrengende.valider())
        addAll(utenlandskNæring.valider())
        addAll(opptjeningIUtlandet.valider())
        addAll(utenlandsoppholdIPerioden.valider())

        frilans?.let { addAll(it.valider()) }
        selvstendigNæringsdrivende?.let { addAll(it.valider()) }

        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true.")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true.")
        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

    internal fun somK9Format(søker: Søker): K9Søknad {
        val ytelse = PleipengerLivetsSluttfase()
            .medSøknadsperiode(Periode(fraOgMed, tilOgMed))
            .medPleietrengende(pleietrengende.somK9Pleietrengende())
            .medBosteder(medlemskap.somK9Bosteder())
            .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
            .medUttak(byggK9Uttak())
            .medArbeidstid(byggK9Arbeidstid())

        if(utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true){
            ytelse.medUtenlandsopphold(utenlandsoppholdIPerioden.somK9Utenlandsopphold())
        }

        return K9Søknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSøknadId(SøknadId(søknadId))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
    }

    private fun byggK9Uttak() = Uttak().medPerioder(mapOf(Periode(fraOgMed, tilOgMed) to UttakPeriodeInfo(SYV_OG_EN_HALV_TIME)))

    private fun byggK9OpptjeningAktivitet() = OpptjeningAktivitet().apply {
        frilans?.let { medFrilanser(it.somK9Frilanser()) }
        this@Søknad.selvstendigNæringsdrivende?.let { medSelvstendigNæringsdrivende(it.somK9SelvstendigNæringsdrivende()) }
    }

    private fun byggK9Arbeidstid() = Arbeidstid().apply {
        if(arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.somK9Arbeidstaker(fraOgMed, tilOgMed))

        selvstendigNæringsdrivende?.let { medSelvstendigNæringsdrivendeArbeidstidInfo(it.somK9ArbeidstidInfo(fraOgMed, tilOgMed)) }

        when(frilans){
            null -> medFrilanserArbeidstid(arbeidstidInfoMedNullTimer(fraOgMed, tilOgMed))
            else -> medFrilanserArbeidstid(frilans.somK9Arbeidstid(fraOgMed, tilOgMed))
        }
    }
}