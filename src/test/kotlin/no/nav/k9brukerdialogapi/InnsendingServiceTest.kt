package no.nav.k9brukerdialogapi

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.MeldingRegistreringFeiletException
import no.nav.k9brukerdialogapi.innsending.InnsendingService
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import no.nav.k9brukerdialogapi.vedlegg.Vedlegg
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Arbeidsgiver
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.DineBarn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.OmsorgspengerutbetalingArbeidstakerSøknad
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Utbetalingsårsak
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.BarnSammeAdresse
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnSøknad
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.gyldigPILSSøknad
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

internal class InnsendingServiceTest {
    @RelaxedMockK
    lateinit var kafkaProducer: KafkaProducer

    @RelaxedMockK
    lateinit var søkerService: SøkerService

    @RelaxedMockK
    lateinit var barnService: BarnService

    @RelaxedMockK
    lateinit var vedleggService: VedleggService
    lateinit var innsendingService: InnsendingService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        innsendingService = InnsendingService(søkerService, kafkaProducer, vedleggService)

        assertNotNull(kafkaProducer)
        assertNotNull(innsendingService)
    }

    @Test
    internal fun `Verifiser at søknadservice for ettersending fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery { søkerService.hentSøker(any(), any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {
                    vedleggService.hentVedlegg(
                        vedleggUrls = any(),
                        any(),
                        any()
                    )
                } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every {
                    kafkaProducer.produserKafkaMelding(
                        any(),
                        any(),
                        any()
                    )
                } throws Exception("Mocket feil ved kafkaProducer")

                innsendingService.registrer(
                    innsending = no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse(
                        språk = "nb",
                        mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
                        vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                        søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                        beskrivelse = "Pleiepenger .....",
                        ettersendelsesType = EttersendelseType.ANNET,
                        harBekreftetOpplysninger = true,
                        harForståttRettigheterOgPlikter = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123",
                        soknadDialogCommitSha = "abc-123"
                    ),
                    idToken = IdToken(
                        Azure.V2_0.generateJwt(
                            clientId = "ikke-authorized-client",
                            audience = "omsorgsdager-melding-api"
                        )
                    ),
                    callId = CallId("abc"),
                    ytelse = Ytelse.ETTERSENDING
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgspenger utvidet rett fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery { søkerService.hentSøker(any(), any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {
                    vedleggService.hentVedlegg(
                        vedleggUrls = any(),
                        any(),
                        any()
                    )
                } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every {
                    kafkaProducer.produserKafkaMelding(
                        any(),
                        any(),
                        any()
                    )
                } throws Exception("Mocket feil ved kafkaProducer")

                innsendingService.registrer(
                    innsending = OmsorgspengerKroniskSyktBarnSøknad(
                        språk = "nb",
                        kroniskEllerFunksjonshemming = true,
                        barn = Barn(
                            norskIdentifikator = "03028104560",
                            navn = "Barn Barnesen"
                        ),
                        relasjonTilBarnet = SøkerBarnRelasjon.FAR,
                        sammeAdresse = BarnSammeAdresse.JA,
                        legeerklæring = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                        samværsavtale = listOf(),
                        harBekreftetOpplysninger = true,
                        harForståttRettigheterOgPlikter = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123",
                        soknadDialogCommitSha = "abc-123"
                    ),
                    idToken = IdToken(
                        Azure.V2_0.generateJwt(
                            clientId = "ikke-authorized-client",
                            audience = "omsorgsdager-melding-api"
                        )
                    ),
                    callId = CallId("abc"),
                    ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgspenger utbetaling arbeidstaker fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery { søkerService.hentSøker(any(), any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {
                    vedleggService.hentVedlegg(
                        vedleggUrls = any(),
                        any(),
                        any()
                    )
                } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every {
                    kafkaProducer.produserKafkaMelding(
                        any(),
                        any(),
                        any()
                    )
                } throws Exception("Mocket feil ved kafkaProducer")

                innsendingService.registrer(
                    innsending = OmsorgspengerutbetalingArbeidstakerSøknad(
                        språk = "nb",
                        vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                        bosteder = listOf(),
                        opphold = listOf(),
                        bekreftelser = Bekreftelser(
                            harBekreftetOpplysninger = true,
                            harForståttRettigheterOgPlikter = true
                        ),
                        arbeidsgivere = listOf(
                            Arbeidsgiver(
                                navn = "Kiwi AS",
                                organisasjonsnummer = "825905162",
                                utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                                konfliktForklaring = "Fordi blablabla",
                                harHattFraværHosArbeidsgiver = true,
                                arbeidsgiverHarUtbetaltLønn = true,
                                perioder = listOf(
                                    Utbetalingsperiode(
                                        fraOgMed = LocalDate.now().minusDays(1),
                                        tilOgMed = LocalDate.now(),
                                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                                    )
                                )
                            )
                        ),
                        dineBarn = DineBarn(
                            harDeltBosted = false,
                            barn = listOf(
                                no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Barn(
                                    identitetsnummer = "11223344567",
                                    aktørId = "1234567890",
                                    LocalDate.now(),
                                    "Barn Barnesen",
                                    no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.TypeBarn.FRA_OPPSLAG
                                )
                            ),
                        ),
                        hjemmePgaSmittevernhensyn = true,
                        hjemmePgaStengtBhgSkole = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123",
                        soknadDialogCommitSha = "abc-123"
                    ),
                    idToken = IdToken(
                        Azure.V2_0.generateJwt(
                            clientId = "authorized-client",
                            audience = "omsorgsdager-melding-api"
                        )
                    ),
                    callId = CallId("abc"),
                    ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgspenger utbetaling snf fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery { søkerService.hentSøker(any(), any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {
                    vedleggService.hentVedlegg(
                        vedleggUrls = any(),
                        any(),
                        any()
                    )
                } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every {
                    kafkaProducer.produserKafkaMelding(
                        any(),
                        any(),
                        any()
                    )
                } throws Exception("Mocket feil ved kafkaProducer")

                innsendingService.registrer(
                    innsending = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.OmsorgspengerutbetalingSnfSøknad(
                        språk = "nb",
                        bosteder = listOf(),
                        opphold = listOf(),
                        spørsmål = listOf(),
                        harDekketTiFørsteDagerSelv = null,
                        bekreftelser = Bekreftelser(
                            harBekreftetOpplysninger = true,
                            harForståttRettigheterOgPlikter = true
                        ),
                        utbetalingsperioder = listOf(
                            Utbetalingsperiode(
                                fraOgMed = LocalDate.parse("2022-01-11"),
                                tilOgMed = LocalDate.parse("2022-01-15"),
                                årsak = FraværÅrsak.SMITTEVERNHENSYN,
                                aktivitetFravær = listOf(
                                    AktivitetFravær.FRILANSER
                                )
                            )
                        ),
                        erArbeidstakerOgså = false,
                        barn = listOf(
                            no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn(
                                navn = "Barnesen",
                                fødselsdato = LocalDate.now().minusYears(14),
                                type = TypeBarn.FRA_OPPSLAG,
                                identitetsnummer = "26104500284"
                            )
                        ),
                        vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123",
                        soknadDialogCommitSha = "abc-123"
                    ),
                    idToken = IdToken(
                        Azure.V2_0.generateJwt(
                            clientId = "authorized-client",
                            audience = "k9-brukerdialog-api"
                        )
                    ),
                    callId = CallId("abc"),
                    ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }


    @Test
    internal fun `Verifiser at søknadservice for pleiepenger livets sluttfase fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery { søkerService.hentSøker(any(), any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {
                    vedleggService.hentVedlegg(
                        vedleggUrls = any(),
                        any(),
                        any()
                    )
                } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every {
                    kafkaProducer.produserKafkaMelding(
                        any(),
                        any(),
                        any()
                    )
                } throws Exception("Mocket feil ved kafkaProducer")

                innsendingService.registrer(
                    innsending = gyldigPILSSøknad(listOf()),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123",
                        soknadDialogCommitSha = "abc-123"
                    ),
                    idToken = IdToken(
                        Azure.V2_0.generateJwt(
                            clientId = "authorized-client",
                            audience = "k9-brukerdialog-api"
                        )
                    ),
                    callId = CallId("abc"),
                    ytelse = Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for pleiepenger sykt barn fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery { søkerService.hentSøker(any(), any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {
                    vedleggService.hentVedlegg(
                        vedleggUrls = any(),
                        any(),
                        any()
                    )
                } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every {
                    kafkaProducer.produserKafkaMelding(
                        any(),
                        any(),
                        any()
                    )
                } throws Exception("Mocket feil ved kafkaProducer")

                innsendingService.registrer(
                    innsending = no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.SøknadUtils.defaultSøknad().copy(
                        fraOgMed = LocalDate.parse("2021-01-01"),
                        tilOgMed = LocalDate.parse("2022-01-10"),
                        fødselsattestVedleggUrls = listOf()
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123",
                        soknadDialogCommitSha = "abc-123"
                    ),
                    idToken = IdToken(
                        Azure.V2_0.generateJwt(
                            clientId = "authorized-client",
                            audience = "k9-brukerdialog-api"
                        )
                    ),
                    callId = CallId("abc"),
                    ytelse = Ytelse.PLEIEPENGER_SYKT_BARN
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

}
