package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode.Companion.somK9FraværPeriode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode.Companion.valider
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
    companion object {
        internal fun List<Arbeidsgiver>.somK9Fraværsperiode() = this.flatMap { it.somK9Fraværsperiode() }
        internal fun List<Arbeidsgiver>.valider(felt: String) = this.flatMapIndexed { index, arbeidsgiver ->
            arbeidsgiver.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(perioder.valider("$felt.perioder"))
        krever(perioder.isNotEmpty(), "$felt.periode kan ikke være tom")
        krever(navn.isNotBlank(), "$felt.navn kan ikke være blankt eller tomt. navn='$navn'")
        krever(organisasjonsnummer.isNotBlank(), "$felt.organisasjonsnummer kan ikke være blankt eller tomt. organisasjonsnummer='$organisasjonsnummer'")
        kreverIkkeNull(arbeidsgiverHarUtbetaltLønn, "$felt.arbeidsgiverHarUtbetaltLønn må være satt")
        kreverIkkeNull(harHattFraværHosArbeidsgiver, "$felt.harHattFraværHosArbeidsgiver må være satt")
        when(utbetalingsårsak){
            NYOPPSTARTET_HOS_ARBEIDSGIVER -> kreverIkkeNull(årsakNyoppstartet, "$felt.årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER")
            KONFLIKT_MED_ARBEIDSGIVER -> krever(!konfliktForklaring.isNullOrBlank(), "$felt.konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER")
            Utbetalingsårsak.ARBEIDSGIVER_KONKURS -> {}
        }
    }

    internal fun somK9Fraværsperiode() = perioder.somK9FraværPeriode(utbetalingsårsak.somSøknadÅrsak(), organisasjonsnummer)
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
