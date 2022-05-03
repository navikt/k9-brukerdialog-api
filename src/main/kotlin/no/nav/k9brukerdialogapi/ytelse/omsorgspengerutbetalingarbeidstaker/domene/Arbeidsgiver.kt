package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

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
    init {
        require(perioder.isNotEmpty()) { "Må inneholde minst en periode." }
        require(navn.isNotBlank()) { "navn kan ikke være blankt eller tomt." }
        require(organisasjonsnummer.isNotBlank()) { "organisasjonsnummer kan ikke være blankt eller tomt." }
        requireNotNull(arbeidsgiverHarUtbetaltLønn) { "arbeidsgiverHarUtbetaltLønn må være satt" }
        requireNotNull(harHattFraværHosArbeidsgiver) { "harHattFraværHosArbeidsgiver må være satt" }

        when (this.utbetalingsårsak) {
            Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER -> {
                requireNotNull(årsakNyoppstartet) { "årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER." }
            }
            Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER -> {
                require(!konfliktForklaring.isNullOrBlank()) { "konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER." }
            }
        }
    }
}

enum class Utbetalingsårsak {
    ARBEIDSGIVER_KONKURS,
    NYOPPSTARTET_HOS_ARBEIDSGIVER,
    KONFLIKT_MED_ARBEIDSGIVER
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