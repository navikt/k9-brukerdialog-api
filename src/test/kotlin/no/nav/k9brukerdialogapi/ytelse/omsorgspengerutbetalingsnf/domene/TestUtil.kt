package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import java.net.URL
import java.time.LocalDate

internal fun genererSøknadForOmsUtSnf(
    harDekketTiFørsteDagerSelv: Boolean = true,
    barn: List<Barn> = listOf(
        Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.now().minusYears(14),
            type = TypeBarn.FRA_OPPSLAG,
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
    selvstendigNæringsdrivende: Virksomhet = Virksomhet(
        fraOgMed = LocalDate.parse("2022-01-01"),
        tilOgMed = LocalDate.parse("2022-10-01"),
        næringstype = Næringstype.DAGMAMMA,
        navnPåVirksomheten = "Kiwi ASA",
        organisasjonsnummer = "975959171",
        registrertINorge = true,
        erNyoppstartet = true,
        harFlereAktiveVirksomheter = false
    ),
    vedlegg: List<URL> = listOf()
) = OmsorgspengerutbetalingSnfSøknad(
    språk = "nb",
    bosteder = bosteder,
    opphold = opphold,
    harDekketTiFørsteDagerSelv = harDekketTiFørsteDagerSelv,
    harSyktBarn = true,
    harAleneomsorg = true,
    spørsmål = listOf(
        SpørsmålOgSvar("Har du hund?", true)
    ),
    bekreftelser = bekreftelser,
    utbetalingsperioder = utbetalingsperiode,
    erArbeidstakerOgså = false,
    barn = barn,
    frilans = frilans,
    selvstendigNæringsdrivende = selvstendigNæringsdrivende,
    vedlegg = vedlegg
)
