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
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
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

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        omsorgspengerUtvidetRettSøknadService = OmsorgspengerUtvidetRettService(
            kafkaProdusent = kafkaProducer,
            vedleggService = vedleggService,
            søkerService = søkerService,
            barnService = barnService
        )
        ettersendingSøknadService = EttersendingService(
            kafkaProducer, søkerService, vedleggService
        )
        assertNotNull(kafkaProducer)
        assertNotNull(omsorgspengerUtvidetRettSøknadService)
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

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

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

                coEvery { vedleggService.hentVedlegg(vedleggUrls = any(), any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

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
}