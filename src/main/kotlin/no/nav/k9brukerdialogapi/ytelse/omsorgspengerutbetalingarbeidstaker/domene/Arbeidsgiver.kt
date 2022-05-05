package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9.søknad.felles.fravær.AktivitetFravær
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER

class Arbeidsgiver(
    private val navn: String,
    private val organisasjonsnummer: String,
    private val utbetalingsårsak: Utbetalingsårsak,
    private val perioder: List<Utbetalingsperiode>,
    private val konfliktForklaring: String? = null,
    private val årsakNyoppstartet: ÅrsakNyoppstartet? = null,
    private val arbeidsgiverHarUtbetaltLønn: Boolean? = null,
    private val harHattFraværHosArbeidsgiver: Boolean? = null
) {
    companion object{
        fun List<Arbeidsgiver>.somK9Fraværsperiode() = this.flatMap { it.somK9Fraværsperiode() }
    }

    init {
        require(perioder.isNotEmpty()) { "Må inneholde minst en periode." }
        require(navn.isNotBlank()) { "navn kan ikke være blankt eller tomt." }
        require(organisasjonsnummer.isNotBlank()) { "organisasjonsnummer kan ikke være blankt eller tomt." }
        requireNotNull(arbeidsgiverHarUtbetaltLønn) { "arbeidsgiverHarUtbetaltLønn må være satt." }
        requireNotNull(harHattFraværHosArbeidsgiver) { "harHattFraværHosArbeidsgiver må være satt." }
        if(utbetalingsårsak == NYOPPSTARTET_HOS_ARBEIDSGIVER){
            requireNotNull(årsakNyoppstartet) { "årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER." }
        }
        if(utbetalingsårsak == KONFLIKT_MED_ARBEIDSGIVER){
            require(!konfliktForklaring.isNullOrBlank()) { "konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER." }
        }
    }

    internal fun somK9Fraværsperiode() = perioder.map {
        it.somFraværPeriode(
            søknadÅrsak = utbetalingsårsak.somSøknadÅrsak(),
            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER),
            organisasjonsnummer = Organisasjonsnummer.of(organisasjonsnummer)
        )
    }
}

enum class Utbetalingsårsak {
    ARBEIDSGIVER_KONKURS,
    NYOPPSTARTET_HOS_ARBEIDSGIVER,
    KONFLIKT_MED_ARBEIDSGIVER;

    fun somSøknadÅrsak() = when(this){
        ARBEIDSGIVER_KONKURS -> SøknadÅrsak.ARBEIDSGIVER_KONKURS
        NYOPPSTARTET_HOS_ARBEIDSGIVER -> SøknadÅrsak.NYOPPSTARTET_HOS_ARBEIDSGIVER
        KONFLIKT_MED_ARBEIDSGIVER -> SøknadÅrsak.KONFLIKT_MED_ARBEIDSGIVER
    }

}

enum class ÅrsakNyoppstartet{
    JOBBET_HOS_ANNEN_ARBEIDSGIVER,
    VAR_FRILANSER,
    VAR_SELVSTENDIGE,
    SØKTE_ANDRE_UTBETALINGER,
    ARBEID_I_UTLANDET,
    UTØVDE_VERNEPLIKT,
    ANNET
}