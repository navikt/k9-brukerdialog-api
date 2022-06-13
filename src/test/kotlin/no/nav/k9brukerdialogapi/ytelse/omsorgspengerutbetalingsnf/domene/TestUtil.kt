package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsperiode
import java.net.URL
import java.time.LocalDate

internal fun genererSøknadForOmsUtSnf(
    harDekketTiFørsteDagerSelv: Boolean = true,
    barn: List<Barn> = listOf(
        Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.now().minusYears(14),
            type = TypeBarn.FRA_OPPSLAG,
            utvidetRett = true,
            identitetsnummer = "26104500284"
        )
    ),
    utbetalingsperiode: List<Utbetalingsperiode> = listOf(
        Utbetalingsperiode(
            fraOgMed = LocalDate.parse("2022-01-20"),
            tilOgMed = LocalDate.parse("2022-01-25"),
            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
            aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
        )
    ),
    bosteder: List<Bosted> = listOf(),
    opphold: List<Opphold> = listOf(),
    bekreftelser: Bekreftelser = Bekreftelser(
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    ),
    frilans: Frilans = Frilans(
        startdato = LocalDate.parse("2022-01-01"),
        sluttdato = LocalDate.parse("2022-10-01"),
        jobberFortsattSomFrilans = true
    ),
    selvstendigNæringsdrivende: SelvstendigNæringsdrivende = SelvstendigNæringsdrivende(
        fraOgMed = LocalDate.parse("2022-01-01"),
        tilOgMed = LocalDate.parse("2022-10-01"),
        næringstyper = listOf(Næringstyper.DAGMAMMA),
        navnPåVirksomheten = "Kiwi ASA",
        organisasjonsnummer = "975959171",
        registrertINorge = true,
        erNyoppstartet = true,
        harFlereAktiveVirksomheter = false
    ),
    vedlegg: List<URL> = listOf()
) = Søknad(
    språk = "nb",
    bosteder = bosteder,
    opphold = opphold,
    harDekketTiFørsteDagerSelv = harDekketTiFørsteDagerSelv,
    spørsmål = listOf(
        SpørsmålOgSvar("Har du hund?", true)
    ),
    bekreftelser = bekreftelser,
    utbetalingsperioder = utbetalingsperiode,
    andreUtbetalinger = listOf(AndreUtbetalinger.DAGPENGER),
    erArbeidstakerOgså = false,
    barn = barn,
    frilans = frilans,
    selvstendigNæringsdrivende = selvstendigNæringsdrivende,
    vedlegg = vedlegg
)