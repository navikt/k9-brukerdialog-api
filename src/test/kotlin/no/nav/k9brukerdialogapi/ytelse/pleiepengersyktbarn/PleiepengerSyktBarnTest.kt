package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.prometheus.client.CollectorRegistry
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.KafkaWrapper
import no.nav.k9brukerdialogapi.PLEIEPENGER_SYKT_BARN_URL
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.jpegUrl
import no.nav.k9brukerdialogapi.somJson
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.BarnDetaljer
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Ferieuttak
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.FerieuttakIPerioden
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidsRedusert
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.ÅrsakManglerIdentitetsnummer
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PleiepengerSyktBarnTest {

    private companion object{
        private val logger: Logger = LoggerFactory.getLogger(PleiepengerSyktBarnTest::class.java)
        val mockOAuth2Server = MockOAuth2Server().apply { start() }
        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .withTokendingsSupport()
            .k9BrukerdialogApiConfig()
            .build()
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9Mellomlagring()

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaKonsumer = kafkaEnvironment.testConsumer()

        private val gyldigFødselsnummerA = "25037139184"
        private val fnrMedBarn = "26104500284"
        private val fnrMedToArbeidsforhold = "19116812889"
        private val ikkeMyndigFnr = "12125012345"
        private val tokenXToken = mockOAuth2Server.issueToken(fnr = gyldigFødselsnummerA)

        fun getConfig(): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment,
                    mockOAuth2Server = mockOAuth2Server
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }

        val engine = TestApplicationEngine(createTestEnvironment { config = getConfig() })

        @BeforeAll
        @JvmStatic
        fun buildUp() {
            CollectorRegistry.defaultRegistry.clear()
            engine.start(wait = true)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wireMockServer.stop()
            mockOAuth2Server.shutdown()
            kafkaEnvironment.stop()
        }
    }

    @Test
    fun `Sende søknad`() {
        val jpegUrl = URL(engine.jpegUrl(jwtToken = tokenXToken))
        val opplastetIdVedlegg = URL(engine.jpegUrl(jwtToken = tokenXToken))

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true,
                ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2022-01-01"),
                        tilOgMed = LocalDate.parse("2022-01-02"),
                    )
                )
            ),
            vedlegg = listOf(jpegUrl),
            fødselsattestVedleggUrls = listOf(opplastetIdVedlegg)
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + INNSENDING_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson(),
            engine = engine,
            logger = logger
        )

        hentOgAssertSøknad(JSONObject(søknad.somJson()))
    }

    @Test
    fun `Sende søknad med AktørID som ID på barnet`() {
        val jpegUrl = engine.jpegUrl(jwtToken = tokenXToken)
        val opplastetIdVedlegg = engine.jpegUrl(jwtToken = tokenXToken)
        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.now().minusDays(3),
            tilOgMed = LocalDate.now().plusDays(4),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(harInntektSomSelvstendig = false),
            omsorgstilbud = null,
            vedlegg = listOf(URL(jpegUrl)),
            fødselsattestVedleggUrls = listOf(URL(opplastetIdVedlegg)),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true,
                ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(2),
                    )
                )
            ),
            barn = BarnDetaljer(
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen",
                aktørId = "1000000000001",
                fødselsnummer = null,
                årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BARNET_BOR_I_UTLANDET
            )
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + INNSENDING_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            jwtToken = tokenXToken,
            requestEntity = søknad.somJson(),
            engine = engine,
            logger = logger
        )

        hentOgAssertSøknad(JSONObject(søknad))
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som ikke er gyldig, mangler registrertILand og ugyldig arbeidsforhold`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + INNSENDING_URL,
            engine = engine,
            logger = logger,
            //language=json
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    "selvstendigNæringsdrivende.arbeidsforhold.arbeidIPeriode.redusertArbeid.prosentAvNormalt må være satt dersom type=PROSENT_AV_NORMALT",
                    "selvstendigNæringsdrivende.virksomhet.nyOppstartet er false. selvstendigNæringsdrivende.virksomhet.fraOgMed må være over 4 år siden",
                    "selvstendigNæringsdrivende.virksomhet.registrertIUtlandet kan ikke være null når selvstendigNæringsdrivende.virksomhet.registrertINorge er false"
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            requestEntity = SøknadUtils.defaultSøknad().copy(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10"),
                ferieuttakIPerioden = null,
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    harInntektSomSelvstendig = true,
                    virksomhet = Virksomhet(
                        næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                        fiskerErPåBladB = false,
                        fraOgMed = LocalDate.parse("2021-02-07"),
                        tilOgMed = LocalDate.parse("2021-02-08"),
                        næringsinntekt = 1233123,
                        navnPåVirksomheten = "TullOgTøys",
                        registrertINorge = false,
                        organisasjonsnummer = "101010",
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                        regnskapsfører = Regnskapsfører(
                            navn = "Kjell",
                            telefon = "84554"
                        ),
                        harFlereAktiveVirksomheter = true,
                        erNyoppstartet = false
                    ),
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            timerPerUkeISnitt = Duration.ZERO
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                            redusertArbeid = ArbeidsRedusert(
                                type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                                prosentAvNormalt = null
                            )
                        )
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad som har satt erBarnetInnlagt til true men har ikke oppgitt noen perioder i perioderBarnetErInnlagt`() {
        val jpegUrl = engine.jpegUrl(jwtToken = tokenXToken)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + INNSENDING_URL,
            engine = engine,
            logger = logger,
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    "utenlandsoppholdIPerioden.opphold[1].perioderBarnetErInnlagt kan ikke være tom når utenlandsoppholdIPerioden.opphold[1].erBarnetInnlagt er true"
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            requestEntity =
            //language=JSON
            """
                {
                  "new_version": true,
                  "sprak": "nb",
                  "barn": {
                    "navn": null,
                    "fødselsnummer": "03028104560",
                    "aktørId": null,
                    "fodselsdato": null
                  },
                  "arbeidsgivere" : [],
                  "frilans" : {
                    "harInntektSomFrilans": false
                  },
                  "selvstendigNæringsdrivende": {
                    "harInntektSomSelvstendig": false
                  },
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": false,
                    "skalBoIUtlandetNeste12Mnd": false,
                    "utenlandsoppholdSiste12Mnd": [
                      
                    ],
                    "utenlandsoppholdNeste12Mnd": [
                      
                    ]
                  },
                  "fraOgMed": "2020-02-01",
                  "tilOgMed": "2020-02-13",
                  "vedlegg": [
                    "$jpegUrl"
                  ],
                  "harMedsøker": false,
                  "opptjeningIUtlandet": [],
                  "utenlandskNæring": [],
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "utenlandsoppholdIPerioden" : 
                    {
                      "skalOppholdeSegIUtlandetIPerioden": true,
                      "opphold": [
                        {
                          "fraOgMed": "2019-10-10",
                          "tilOgMed": "2019-11-10",
                          "landkode": "SE",
                          "landnavn": "Sverige",
                          "erUtenforEøs": false,
                          "erBarnetInnlagt": false
                        },
                        {
                          "landnavn": "USA",
                          "landkode": "US",
                          "fraOgMed": "2020-01-08",
                          "tilOgMed": "2020-01-09",
                          "erUtenforEøs": true,
                          "erBarnetInnlagt": true,
                          "perioderBarnetErInnlagt": [],
                          "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                        }
                      ]
                    },
                    "harVærtEllerErVernepliktig" : true
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende soknad hvor et av vedleggene peker på et ikke eksisterende vedlegg`() {
        val jpegUrl = engine.jpegUrl(jwtToken = tokenXToken)
        val finnesIkkeUrl = jpegUrl.substringBeforeLast("/").plus("/").plus(UUID.randomUUID().toString())

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + INNSENDING_URL,
            expectedResponse = """
            {
                "type": "/problem-details/invalid-request-parameters",
                "title": "invalid-request-parameters",
                "status": 400,
                "detail": "Requesten inneholder ugyldige paramtere.",
                "instance": "about:blank",
                "invalid_parameters": [{
                    "type": "entity",
                    "name": "vedlegg",
                    "reason": "Mottok referanse til 2 vedlegg, men fant kun 1 vedlegg.",
                    "invalid_value": ["$jpegUrl", "$finnesIkkeUrl"]
                }]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            jwtToken = tokenXToken,
            requestEntity = SøknadUtils.defaultSøknad().copy(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10"),
                ferieuttakIPerioden = null,
                vedlegg = listOf(URL(jpegUrl), URL(finnesIkkeUrl)),
                fødselsattestVedleggUrls = listOf()
            ).somJson(),
            engine = engine,
            logger = logger
        )
    }

    private fun hentOgAssertSøknad(søknad: JSONObject) {
        val hentet = kafkaKonsumer.hentSøknad(søknad.getString("søknadId"), Ytelse.PLEIEPENGER_SYKT_BARN)
        assertGyldigSøknad(søknad, hentet.data)
    }

    private fun assertGyldigSøknad(
        søknadSendtInn: JSONObject,
        søknadFraTopic: JSONObject
    ) {
        assertTrue(søknadFraTopic.has("søker"))
        assertTrue(søknadFraTopic.has("mottatt"))
        assertTrue(søknadFraTopic.has("k9FormatSøknad"))

        val k9Format = søknadFraTopic.getJSONObject("k9FormatSøknad")
        assertEquals("PLEIEPENGER_SYKT_BARN", k9Format.getJSONObject("ytelse").getString("type"))

        assertEquals(søknadSendtInn.getString("søknadId"), søknadFraTopic.getString("søknadId"))

        if (søknadSendtInn.has("vedleggUrls") && !søknadSendtInn.getJSONArray("vedleggUrls").isEmpty) {
            assertEquals(
                søknadSendtInn.getJSONArray("vedleggUrls").length(),
                søknadFraTopic.getJSONArray("vedleggUrls").length()
            )
        }
    }
}
