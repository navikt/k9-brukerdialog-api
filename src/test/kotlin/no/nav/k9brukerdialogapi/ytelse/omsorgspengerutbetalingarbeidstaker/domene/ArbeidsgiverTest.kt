package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserFeil
import no.nav.k9brukerdialogapi.TestUtils.Companion.verifiserIngenFeil
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Arbeidsgiver.Companion.somK9Fraværsperiode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class ArbeidsgiverTest {

    @Test
    fun `Gyldig arbeidsgiver uten feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
            konfliktForklaring = "Fordi blablabla",
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = true,
            perioder = listOf(
                Utbetalingsperiode(
                    fraOgMed = LocalDate.now().minusDays(4),
                    tilOgMed = LocalDate.now(),
                    antallTimerBorte = Duration.ofHours(5),
                    antallTimerPlanlagt = Duration.ofHours(7),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            )
        ).valider("arbeidsgiver").verifiserIngenFeil()
    }

    @Test
    fun `Arbeidsgiver uten perioder skal gi feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
            perioder = listOf(),
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = true
        ).valider("arbeidsgiver").verifiserFeil(1, listOf("arbeidsgiver.periode kan ikke være tom"))
    }

    @Test
    fun `Arbeidsgiver med blankt navn skal gi feil`() {
        Arbeidsgiver(
            navn = " ",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
            perioder = listOf(
                Utbetalingsperiode(
                    LocalDate.now(),
                    LocalDate.now().plusDays(4),
                    Duration.ofHours(5),
                    Duration.ofHours(7),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            ),
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = true
        ).valider("arbeidsgiver").verifiserFeil(1, listOf("arbeidsgiver.navn kan ikke være blankt eller tomt. navn=' '"))
    }

    @Test
    fun `Arbeidsgiver med blankt organisasjonsnummer skal gi feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = " ",
            utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
            perioder = listOf(
                Utbetalingsperiode(
                    LocalDate.now(),
                    LocalDate.now().plusDays(4),
                    Duration.ofHours(5),
                    Duration.ofHours(7),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            ),
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = true
        ).valider("arbeidsgiver")
            .verifiserFeil(1, listOf("arbeidsgiver.organisasjonsnummer kan ikke være blankt eller tomt. organisasjonsnummer=' '"))
    }

    @Test
    fun `Arbeidsgiver med årsak KONFLIKT_MED_ARBEIDSGIVER uten forklaring skal gi feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
            konfliktForklaring = null,
            perioder = listOf(
                Utbetalingsperiode(
                    LocalDate.now(),
                    LocalDate.now().plusDays(4),
                    Duration.ofHours(5),
                    Duration.ofHours(7),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            ),
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = true
        ).valider("arbeidsgiver")
            .verifiserFeil(1, listOf("arbeidsgiver.konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER"))
    }

    @Test
    fun `Arbeidsgiver med årsak NYOPPSTARTET_HOS_ARBEIDSGIVER uten årsakNyoppstartet skal gi feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
            årsakNyoppstartet = null,
            perioder = listOf(
                Utbetalingsperiode(
                    LocalDate.now(),
                    LocalDate.now().plusDays(4),
                    Duration.ofHours(5),
                    Duration.ofHours(7),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            ),
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = true
        ).valider("arbeidsgiver")
            .verifiserFeil(1, listOf("arbeidsgiver.årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER"))
    }

    @Test
    fun `Arbeidsgiver med harHattFraværHosArbeidsgiver som null skal gi feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
            årsakNyoppstartet = ÅrsakNyoppstartet.ANNET,
            perioder = listOf(
                Utbetalingsperiode(
                    LocalDate.now(),
                    LocalDate.now().plusDays(4),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            ),
            harHattFraværHosArbeidsgiver = null,
            arbeidsgiverHarUtbetaltLønn = true
        ).valider("arbeidsgiver").verifiserFeil(1, listOf("arbeidsgiver.harHattFraværHosArbeidsgiver må være satt"))
    }

    @Test
    fun `Arbeidsgiver med arbeidsgiverHarUtbetaltLønn som null skal gi feil`() {
        Arbeidsgiver(
            navn = "Kiwi AS",
            organisasjonsnummer = "825905162",
            utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
            årsakNyoppstartet = ÅrsakNyoppstartet.ANNET,
            perioder = listOf(
                Utbetalingsperiode(
                    LocalDate.now(),
                    LocalDate.now().plusDays(4),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            ),
            harHattFraværHosArbeidsgiver = true,
            arbeidsgiverHarUtbetaltLønn = null
        ).valider("arbeidsgiver").verifiserFeil(1, listOf("arbeidsgiver.arbeidsgiverHarUtbetaltLønn må være satt"))
    }

    @Test
    fun `Genererer FraværPerioder som forventet`() {
        val arbeidsgivere = listOf(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                årsakNyoppstartet = ÅrsakNyoppstartet.ANNET,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-01"),
                        tilOgMed = LocalDate.parse("2022-01-05"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    ),
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-10"),
                        tilOgMed = LocalDate.parse("2022-01-15"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ),
            Arbeidsgiver(
                navn = "Rema AS",
                organisasjonsnummer = "883409442",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                årsakNyoppstartet = ÅrsakNyoppstartet.ANNET,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-20"),
                        tilOgMed = LocalDate.parse("2022-01-25"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            )
        )
        assertTrue(arbeidsgivere.somK9Fraværsperiode().size == 3)
    }
}