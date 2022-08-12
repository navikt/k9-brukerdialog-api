package no.nav.k9brukerdialogapi

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.general.MeldingRegistreringFeiletException
import no.nav.k9brukerdialogapi.kafka.KafkaProducer
import no.nav.k9brukerdialogapi.kafka.Metadata
import no.nav.k9brukerdialogapi.oppslag.barn.BarnService
import no.nav.k9brukerdialogapi.oppslag.søker.Søker
import no.nav.k9brukerdialogapi.oppslag.søker.SøkerService
import no.nav.k9brukerdialogapi.vedlegg.DokumentEier
import no.nav.k9brukerdialogapi.vedlegg.Vedlegg
import no.nav.k9brukerdialogapi.vedlegg.VedleggService
import no.nav.k9brukerdialogapi.ytelse.ettersending.EttersendingService
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.*
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.OmsorgsdagerMeldingService
import no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.*
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.OmsorgspengerUtbetalingArbeidstakerService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.*
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.OmsorgspengerUtbetalingSnfService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettService
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.Søknad
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

internal class SøknadServiceTest{
    @RelaxedMockK
    lateinit var kafkaProducer: KafkaProducer

    @RelaxedMockK
    lateinit var søkerService: SøkerService

    @RelaxedMockK
    lateinit var barnService: BarnService

    @RelaxedMockK
    lateinit var vedleggService: VedleggService

    lateinit var omsorgspengerUtvidetRettSøknadService: OmsorgspengerUtvidetRettService
    lateinit var ettersendingSøknadService: EttersendingService
    lateinit var omsorgspengerUtbetalingArbeidstakerService: OmsorgspengerUtbetalingArbeidstakerService
    lateinit var omsorgspengerUtbetalingSnfService: OmsorgspengerUtbetalingSnfService
    lateinit var omsorgsdagerMeldingService: OmsorgsdagerMeldingService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        omsorgspengerUtvidetRettSøknadService = OmsorgspengerUtvidetRettService(
            søkerService, barnService, vedleggService, kafkaProducer
        )
        ettersendingSøknadService = EttersendingService(
            kafkaProducer, søkerService, vedleggService
        )
        omsorgspengerUtbetalingArbeidstakerService = OmsorgspengerUtbetalingArbeidstakerService(
            søkerService, vedleggService, kafkaProducer
        )
        omsorgspengerUtbetalingSnfService = OmsorgspengerUtbetalingSnfService(
            søkerService, barnService, vedleggService, kafkaProducer
        )
        omsorgsdagerMeldingService = OmsorgsdagerMeldingService(
            søkerService, barnService, kafkaProducer, vedleggService
        )
        assertNotNull(kafkaProducer)
        assertNotNull(omsorgspengerUtvidetRettSøknadService)
        assertNotNull(ettersendingSøknadService)
        assertNotNull(omsorgspengerUtbetalingArbeidstakerService)
        assertNotNull(omsorgspengerUtbetalingSnfService)
        assertNotNull(omsorgsdagerMeldingService)
    }

    @Test
    internal fun `Verifiser at søknadservice for ettersending fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.hentSøker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserKafkaMelding(any(), any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                ettersendingSøknadService.registrer(
                    søknad = no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknad(
                        språk = "nb",
                        mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
                        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
                        søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                        beskrivelse = "Pleiepenger .....",
                        harBekreftetOpplysninger = true,
                        harForståttRettigheterOgPlikter = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "ikke-authorized-client", audience = "omsorgsdager-melding-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgspenger utvidet rett fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.hentSøker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserKafkaMelding(any(), any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                omsorgspengerUtvidetRettSøknadService.registrer(
                    søknad = Søknad(
                        språk = "nb",
                        kroniskEllerFunksjonshemming = true,
                        barn = Barn(
                            norskIdentifikator = "03028104560",
                            navn = "Barn Barnesen"
                        ),
                        relasjonTilBarnet = SøkerBarnRelasjon.FAR,
                        sammeAdresse = true,
                        legeerklæring = listOf(URL("http://localhost:8080/vedlegg/1")),
                        samværsavtale = listOf(),
                        harBekreftetOpplysninger = true,
                        harForståttRettigheterOgPlikter = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "ikke-authorized-client", audience = "omsorgsdager-melding-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgspenger utbetaling arbeidstaker fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.hentSøker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserKafkaMelding(any(), any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                omsorgspengerUtbetalingArbeidstakerService.registrer(
                    søknad = Søknad(
                            språk = "nb",
                            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
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
                                            fraOgMed = LocalDate.now().minusDays(4),
                                            tilOgMed = LocalDate.now(),
                                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                                            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                                        )
                                    )
                                )
                            ),
                            hjemmePgaSmittevernhensyn = true,
                            hjemmePgaStengtBhgSkole = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "authorized-client", audience = "omsorgsdager-melding-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgspenger utbetaling snf fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.hentSøker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserKafkaMelding(any(), any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                omsorgspengerUtbetalingSnfService.registrer(
                    søknad = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Søknad(
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
                        andreUtbetalinger = listOf(),
                        erArbeidstakerOgså = false,
                        barn = listOf(
                            no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Barn(
                                navn = "Barnesen",
                                fødselsdato = LocalDate.now().minusYears(14),
                                type = TypeBarn.FRA_OPPSLAG,
                                utvidetRett = true,
                                identitetsnummer = "26104500284"
                            )
                        ),
                        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "authorized-client", audience = "k9-brukerdialog-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }

    @Test
    internal fun `Verifiser at søknadservice for omsorgsdager-melding fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.hentSøker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserKafkaMelding(any(), any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                omsorgsdagerMeldingService.registrer(
                    melding = Melding(
                        id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                        språk = "nb",
                        mottakerFnr = "26104500284",
                        mottakerNavn = "Navnesen",
                        barn = listOf(
                            no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene.Barn(
                                identitetsnummer = "02119970078",
                                fødselsdato = LocalDate.now(),
                                navn = "Navnesen",
                                aleneOmOmsorgen = true,
                                utvidetRett = true
                            )
                        ),
                        harUtvidetRett = true,
                        harAleneomsorg = true,
                        erYrkesaktiv = true,
                        arbeiderINorge = true,
                        arbeidssituasjon = listOf(Arbeidssituasjon.ARBEIDSTAKER),
                        type = Meldingstype.FORDELING,
                        fordeling = Fordele(Mottaker.SAMVÆRSFORELDER, listOf(URL("http://localhost:8080/vedlegg/1"))),
                        harForståttRettigheterOgPlikter = true,
                        harBekreftetOpplysninger = true
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "authorized-client", audience = "k9-brukerdialog-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }
}