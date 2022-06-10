package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsperiode
import java.time.LocalDate

internal fun genererSøknadForOmsUtSnf(
    barn: List<Barn> = listOf(
        Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.now().minusYears(14),
            type = TypeBarn.FRA_OPPSLAG,
            utvidetRett = false,
            identitetsnummer = "26104500284"
        )
    )
) = Søknad(
    språk = "nb",
    bosteder = listOf(),
    opphold = listOf(),
    spørsmål = listOf(
        SpørsmålOgSvar("Har du hund?", true)
    ),
    bekreftelser = Bekreftelser(
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    ),
    utbetalingsperioder = listOf(
        Utbetalingsperiode(
            fraOgMed = LocalDate.parse("2022-01-20"),
            tilOgMed = LocalDate.parse("2022-01-25"),
            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
            aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
        )
    ),
    andreUtbetalinger = listOf(AndreUtbetalinger.DAGPENGER),
    erArbeidstakerOgså = false,
    barn = barn,
    vedlegg = listOf()
)