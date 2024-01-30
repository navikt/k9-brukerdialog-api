package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.INGEN_ARBEIDSDAG
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object PILSTestUtils {
    val INGEN_ARBEIDSDAG = Duration.ZERO
    val FULL_ARBEIDSDAG = Duration.ofHours(7).plusMinutes(30)
    val HALV_ARBEIDSDAG = Duration.ofHours(3)

    val mandag = LocalDate.parse("2022-08-01")
    val tirsdag = mandag.plusDays(1)
    val onsdag = tirsdag.plusDays(1)
    val torsdag = onsdag.plusDays(1)
    val fredag = torsdag.plusDays(1)

    val enkeltDagerMedJobbSomVanlig = listOf(
        Enkeltdag(mandag, FULL_ARBEIDSDAG),
        Enkeltdag(tirsdag, FULL_ARBEIDSDAG),
        Enkeltdag(onsdag, FULL_ARBEIDSDAG),
        Enkeltdag(torsdag, FULL_ARBEIDSDAG),
        Enkeltdag(fredag, FULL_ARBEIDSDAG),
    )

    val enkeltDagerMedFulltFravær = listOf(
        Enkeltdag(mandag, INGEN_ARBEIDSDAG),
        Enkeltdag(tirsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(onsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(torsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(fredag, INGEN_ARBEIDSDAG),
    )

    val enkeltDagerMedRedusertArbeid = listOf(
        Enkeltdag(mandag, INGEN_ARBEIDSDAG),
        Enkeltdag(tirsdag, HALV_ARBEIDSDAG),
        Enkeltdag(onsdag, FULL_ARBEIDSDAG),
        Enkeltdag(torsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(fredag, FULL_ARBEIDSDAG),
    )
}

internal fun gyldigPILSSøknad(
    vedleggUrls: List<URL> = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
    opplastetIdVedleggUrls: List<URL> = listOf(URI.create("http://localhost:8080/vedlegg/2").toURL()),
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
                    enkeltdager = listOf(
                        Enkeltdag(LocalDate.parse("2021-01-01"), Duration.ofHours(4)),
                        Enkeltdag(LocalDate.parse("2021-01-04"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-05"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-06"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-07"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-08"), INGEN_ARBEIDSDAG),
                    )
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
        arbeidsforhold = Arbeidsforhold(
            37.5, ArbeidIPeriode(
                HELT_FRAVÆR, listOf(
                    Enkeltdag(LocalDate.parse("2021-01-01"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-02"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-03"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-04"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-05"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-06"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-07"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-08"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-09"), INGEN_ARBEIDSDAG),
                    Enkeltdag(LocalDate.parse("2021-01-10"), INGEN_ARBEIDSDAG),
                )
            )
        ),
    ),
    pleierDuDenSykeHjemme: Boolean = true,
) = PilsSøknad(
    søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
    mottatt = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    språk = "nb",
    vedleggUrls = vedleggUrls,
    opplastetIdVedleggUrls = opplastetIdVedleggUrls,
    pleietrengende = pleietrengende,
    fraOgMed = LocalDate.parse("2021-01-01"),
    tilOgMed = LocalDate.parse("2021-01-10"),
    skalJobbeOgPleieSammeDag = true,
    dagerMedPleie = listOf(
        LocalDate.parse("2021-01-01"),
        LocalDate.parse("2021-01-02"),
        LocalDate.parse("2021-01-03"),
        LocalDate.parse("2021-01-04"),
        LocalDate.parse("2021-01-05"),
        LocalDate.parse("2021-01-06"),
        LocalDate.parse("2021-01-07"),
        LocalDate.parse("2021-01-08"),
        LocalDate.parse("2021-01-09"),
        LocalDate.parse("2021-01-10"),
    ),
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
    dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
)
