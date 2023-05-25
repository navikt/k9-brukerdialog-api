package no.nav.k9brukerdialogapi.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.ArbeiderIPeriodenSvar
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.NormalArbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.UkjentArbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.innsyn.InnsynBarn
import no.nav.k9brukerdialogapi.innsyn.K9SakInnsynSøknad
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

fun defaultK9FormatPSB(
    søknadId: UUID = UUID.randomUUID(),
    søknadsPeriode: List<Periode> = listOf(Periode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-01"))),
    arbeidstid: Arbeidstid = Arbeidstid().medArbeidstaker(
        listOf(
            Arbeidstaker()
                .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                .medArbeidstidInfo(
                    ArbeidstidInfo().medPerioder(
                        mapOf(
                            Periode(
                                LocalDate.parse("2018-01-01"),
                                LocalDate.parse("2020-01-05")
                            ) to ArbeidstidPeriodeInfo()
                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                            Periode(
                                LocalDate.parse("2020-01-06"),
                                LocalDate.parse("2020-01-10")
                            ) to ArbeidstidPeriodeInfo()
                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                        )
                    )
                )
        )
    ),
) = Søknad(

    SøknadId.of(søknadId.toString()),
    Versjon.of("1.0.0"),
    ZonedDateTime.parse("2020-01-01T10:00:00Z"),
    Søker(NorskIdentitetsnummer.of("12345678910")),
    PleiepengerSyktBarn()
        .medSøknadsperiode(søknadsPeriode)
        .medSøknadInfo(DataBruktTilUtledning(true, true, true, true, null, true,
            listOf(
                UkjentArbeidsforhold()
                    .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                    .medErAnsatt(true)
                    .medArbeiderIPerioden(ArbeiderIPeriodenSvar.HELT_FRAVÆR)
                    .medNormalarbeidstid(NormalArbeidstid().medTimerPerUke(Duration.ofHours(8)))
            )))
        .medBarn(Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("02119970079")))
        .medArbeidstid(arbeidstid)
)

fun defaultK9SakInnsynSøknad(barn: InnsynBarn, søknad: Søknad) = K9SakInnsynSøknad(
    barn = barn,
    søknad = søknad
)
