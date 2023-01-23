package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.domene.arbeid.NormalArbeidstid
import org.junit.jupiter.api.Assertions
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test

class SoknadValidationTest {

    @Test
    fun `Feiler på søknad dersom utenlandsopphold har til og fra dato som ikke kommer i rett rekkefølge`() {
        Assertions.assertThrows(Throwblem::class.java) {
            val søknad = soknad(
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = false,
                    skalBoIUtlandetNeste12Mnd = true,
                    utenlandsoppholdNeste12Mnd = listOf(
                        Bosted(
                            LocalDate.of(2022, 1, 4),
                            LocalDate.of(2022, 1, 3),
                            "US", "USA"
                        )
                    )
                )
            )
            søknad.valider()
        }
    }

    @Test
    fun `Feiler på søknad dersom utenlandsopphold mangler landkode`() {
        Assertions.assertThrows(Throwblem::class.java) {
            val søknad = soknad(
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = false,
                    skalBoIUtlandetNeste12Mnd = true,
                    utenlandsoppholdNeste12Mnd = listOf(
                        Bosted(
                            LocalDate.of(2022, 1, 2),
                            LocalDate.of(2022, 1, 3),
                            "", "USA"
                        )
                    )
                )
            )
            søknad.valider()
        }
    }

    @Test
    fun `Skal feile dersom barnRelasjon er ANNET men barnRelasjonBeskrivelse er tom`() {
        Assertions.assertThrows(Throwblem::class.java) {
            val søknad = soknad().copy(
                barnRelasjon = BarnRelasjon.ANNET,
                barnRelasjonBeskrivelse = null
            )

            søknad.valider()
        }
    }

    private fun soknad(
        medlemskap: Medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = false,
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Bosted(
                    LocalDate.of(2022, 1, 2),
                    LocalDate.of(2022, 1, 3),
                    "US", "USA"
                )
            )
        ),
    ) = Søknad(
        newVersion = null,
        språk = Språk.nb,
        barn = BarnDetaljer(
            aktørId = null,
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.now(),
            navn = null
        ),
        frilans = Frilans(harInntektSomFrilanser = false),
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(harInntektSomSelvstendig = false),
        opptjeningIUtlandet = listOf(
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
        utenlandskNæring = listOf(),
        arbeidsgivere = listOf(
            Arbeidsgiver(
                navn = "Org",
                organisasjonsnummer = "917755736",
                erAnsatt = true,
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                        arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                    )
                )
            )
        ),
        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
        fødselsattestVedleggUrls = listOf(URL("http://localhost:8080/vedlegg/2")),
        fraOgMed = LocalDate.now(),
        tilOgMed = LocalDate.now(),
        medlemskap = medlemskap,
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = false,
            opphold = listOf()
        ),
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf()),
        barnRelasjon = null,
        barnRelasjonBeskrivelse = null,
        harVærtEllerErVernepliktig = true
        // harHattInntektSomFrilanser = false, default == false
    )
}
