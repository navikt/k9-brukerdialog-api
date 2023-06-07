package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun gyldigPILSSøknad(
    vedleggUrls: List<URL> = listOf(URL("http://localhost:8080/vedlegg/1")),
    opplastetIdVedleggUrls: List<URL> = listOf(URL("http://localhost:8080/vedlegg/2")),
    pleietrengende: Pleietrengende = Pleietrengende(norskIdentitetsnummer = "06098523047", navn = "Bjarne"),
    medlemskap: Medlemskap = Medlemskap(
        harBoddIUtlandetSiste12Mnd = true,
        utenlandsoppholdSiste12Mnd = listOf(
            Utenlandsopphold(
                fraOgMed = LocalDate.parse("2021-01-01"),
                tilOgMed = LocalDate.parse("2021-01-10"),
                landnavn = "Brazil",
                landkode = "BRA"
            )
        ),
        skalBoIUtlandetNeste12Mnd = true,
        utenlandsoppholdNeste12Mnd = listOf(
            Utenlandsopphold(
                fraOgMed = LocalDate.parse("2023-01-01"),
                tilOgMed = LocalDate.parse("2023-01-10"),
                landnavn = "Cuba",
                landkode = "CUB"
            )
        )
    ),
    arbeidsgivere: List<Arbeidsgiver> = listOf(
        Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = true,
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 40.0,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = REDUSERT,
                    enkeltdager = listOf(Enkeltdag(LocalDate.parse("2021-01-01"), Duration.ofHours(4)))
                )
            )
        ),
        Arbeidsgiver(
            navn = "JobberIkkeHerLenger",
            organisasjonsnummer = "977155436",
            erAnsatt = false,
            sluttetFørSøknadsperiode = false
        )
    ),
    opptjeningIUtlandet: List<OpptjeningIUtlandet> = listOf(
        OpptjeningIUtlandet(
            navn = "Kiwi AS",
            opptjeningType = OpptjeningType.ARBEIDSTAKER,
            land = Land(
                landkode = "BEL",
                landnavn = "Belgia",
            ),
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10")
        )
    ),
    utenlandskNæring: List<UtenlandskNæring> = listOf(
        UtenlandskNæring(
            næringstype = Næringstype.FISKE,
            navnPåVirksomheten = "Fiskeriet AS",
            land = Land(
                landkode = "BEL",
                landnavn = "Belgia",
            ),
            fraOgMed = LocalDate.parse("2020-01-01")
        )
    ),
    utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
        skalOppholdeSegIUtlandetIPerioden = true,
        opphold = listOf(
            Utenlandsopphold(
                fraOgMed = LocalDate.parse("2020-02-01"),
                tilOgMed = LocalDate.parse("2020-02-09"),
                landnavn = "Cuba",
                landkode = "CUB"
            )
        )
    ),
    selvstendigNæringsdrivende: SelvstendigNæringsdrivende = SelvstendigNæringsdrivende(
        virksomhet = Virksomhet(
            fraOgMed = LocalDate.parse("2015-01-01"),
            tilOgMed = LocalDate.parse("2021-01-01"),
            næringstype = Næringstype.ANNEN,
            fiskerErPåBladB = true,
            navnPåVirksomheten = "Bjarnes Bakeri",
            registrertINorge = false,
            registrertIUtlandet = Land("CUB", "Cuba"),
            næringsinntekt = 9656876,
            erNyoppstartet = false,
            harFlereAktiveVirksomheter = false
        ),
        arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR))
    ),
    pleierDuDenSykeHjemme: Boolean = true
) = PilsSøknad(
    søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
    mottatt = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    språk = "nb",
    vedleggUrls = vedleggUrls,
    opplastetIdVedleggUrls = opplastetIdVedleggUrls,
    pleietrengende = pleietrengende,
    fraOgMed = LocalDate.parse("2021-01-01"),
    tilOgMed = LocalDate.parse("2021-01-10"),
    medlemskap = medlemskap,
    utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
    arbeidsgivere = arbeidsgivere,
    frilans = Frilans(
        startdato = LocalDate.parse("2019-01-01"),
        jobberFortsattSomFrilans = false,
        sluttdato = LocalDate.parse("2021-05-01"),
        harHattInntektSomFrilanser = false
    ),
    selvstendigNæringsdrivende = selvstendigNæringsdrivende,
    opptjeningIUtlandet = opptjeningIUtlandet,
    utenlandskNæring = utenlandskNæring,
    pleierDuDenSykeHjemme = pleierDuDenSykeHjemme,
    harForståttRettigheterOgPlikter = true,
    harBekreftetOpplysninger = true,
    dataBruktTilUtledning = mutableMapOf(
        "key 1" to "value 1"
    )
)
