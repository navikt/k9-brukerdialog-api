package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.endringsmelding

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.prometheus.client.CollectorRegistry
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogapi.ENDRINGSMELDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.KafkaWrapper
import no.nav.k9brukerdialogapi.PLEIEPENGER_SYKT_BARN_URL
import no.nav.k9brukerdialogapi.TestConfiguration
import no.nav.k9brukerdialogapi.TestUtils.Companion.issueToken
import no.nav.k9brukerdialogapi.TestUtils.Companion.requestAndAssert
import no.nav.k9brukerdialogapi.hentSøknad
import no.nav.k9brukerdialogapi.innsyn.InnsynBarn
import no.nav.k9brukerdialogapi.testConsumer
import no.nav.k9brukerdialogapi.utils.defaultK9FormatPSB
import no.nav.k9brukerdialogapi.utils.defaultK9SakInnsynSøknad
import no.nav.k9brukerdialogapi.wiremock.k9BrukerdialogApiConfig
import no.nav.k9brukerdialogapi.wiremock.stubK9Mellomlagring
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagBarn
import no.nav.k9brukerdialogapi.wiremock.stubK9OppslagSoker
import no.nav.k9brukerdialogapi.wiremock.stubSifInnsynApi
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class EndringsmeldingPleiepengerSyktBarnTest {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EndringsmeldingPleiepengerSyktBarnTest::class.java)
        private val søkerMedBarn = "02119970078"
        private val barnIdentitetsnummer = "18909798651"

        val mockOAuth2Server = MockOAuth2Server().apply { start() }

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withLoginServiceSupport()
            .withTokendingsSupport()
            .k9BrukerdialogApiConfig()
            .build()
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9Mellomlagring()
            .stubSifInnsynApi(
                k9SakInnsynSøknader = listOf(
                    defaultK9SakInnsynSøknad(
                        barn = InnsynBarn(
                            fødselsdato = LocalDate.parse("2000-08-27"),
                            fornavn = "BARNESEN",
                            mellomnavn = "EN",
                            etternavn = "BARNESEN",
                            aktørId = "1000000000001",
                            identitetsnummer = barnIdentitetsnummer
                        ),
                        søknad = defaultK9FormatPSB()
                    )
                )
            )
        private val kafkaEnvironment = KafkaWrapper.bootstrap()

        private val kafkaKonsumer = kafkaEnvironment.testConsumer()

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
    fun `endringsmelding - endringer innefor gyldighetsperiode`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        //language=json
        val endringsmelding = """
            {
             "søknadId": "$søknadId",
              "id": "123",
              "språk": "nb",
              "mottattDato": "$mottattDato",
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ytelse": {
                "type": "PLEIEPENGER_SYKT_BARN",
                "barn": {
                  "norskIdentitetsnummer": "$barnIdentitetsnummer"
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "organisasjonsnummer": "917755736",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-01": {
                            "jobberNormaltTimerPerDag": "PT1H0M",
                            "faktiskArbeidTimerPerDag": "PT0H"
                          }
                        }
                      }
                    }
                  ]
                },
                "tilsynsordning": {
                  "perioder": {
                    "2021-01-01/2021-01-01": {
                      "etablertTilsynTimerPerDag": "PT2H0M"
                    }
                  }
                }
              }
            }
        """.trimIndent()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + ENDRINGSMELDING_URL + INNSENDING_URL,
            jwtToken = mockOAuth2Server.issueToken(fnr = søkerMedBarn),
            expectedCode = HttpStatusCode.Accepted,
            expectedResponse = null,
            requestEntity = endringsmelding,
            engine = engine,
            logger = logger
        )

        hentOgAsserEndringsmelding(
            //language=json
            """
           {
             "søknadId": "$søknadId",
             "søker": {
               "mellomnavn": "HEISANN",
               "etternavn": "MORSEN",
               "aktørId": "12345",
               "fødselsdato": "1999-11-02",
               "fornavn": "MOR",
               "fødselsnummer": "$søkerMedBarn"
             },
             "harBekreftetOpplysninger": true,
             "harForståttRettigheterOgPlikter": true,
             "k9Format": {
               "søknadId": "$søknadId",
               "versjon": "1.0.0",
               "mottattDato": "$mottattDato",
               "språk": "nb",
               "søker": {
                 "norskIdentitetsnummer": "$søkerMedBarn"
               },
               "ytelse": {
                 "type": "PLEIEPENGER_SYKT_BARN",
                 "søknadsperiode": [],
                 "endringsperiode": [],
                 "trekkKravPerioder": [],
                 "barn": {
                   "norskIdentitetsnummer": "$barnIdentitetsnummer",
                   "fødselsdato": null
                 },
                 "tilsynsordning": {
                   "perioder": {
                     "2021-01-01/2021-01-01": {
                       "etablertTilsynTimerPerDag": "PT2H"
                     }
                   }
                 },
                 "arbeidstid": {
                   "frilanserArbeidstidInfo": null,
                   "arbeidstakerList": [
                     {
                       "organisasjonsnummer": "917755736",
                       "norskIdentitetsnummer": null,
                       "arbeidstidInfo": {
                         "perioder": {
                           "2021-01-01/2021-01-01": {
                             "faktiskArbeidTimerPerDag": "PT0S",
                             "jobberNormaltTimerPerDag": "PT1H"
                           }
                         }
                       }
                     }
                   ],
                   "selvstendigNæringsdrivendeArbeidstidInfo": null
                 },
                 "bosteder": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "lovbestemtFerie": {
                   "perioder": {}
                 },
                 "omsorg": {
                   "beskrivelseAvOmsorgsrollen": null,
                   "relasjonTilBarnet": null
                 },
                 "utenlandsopphold": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "nattevåk": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "infoFraPunsj": null,
                 "dataBruktTilUtledning": null,
                 "beredskap": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "uttak": {
                   "perioder": {}
                 },
                 "opptjeningAktivitet": {}
               },
               "journalposter": [],
               "begrunnelseForInnsending": {
                 "tekst": null
               }
             }
           }
            """.trimIndent(),
            JSONObject(endringsmelding)
        )
    }

    @Test
    fun `endringsmelding - endringer utenfor gyldighetsperiode`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        //language=json
        val endringsmelding = """
                {
                  "søknadId": "$søknadId",
                  "språk": "nb",
                  "mottattDato": "$mottattDato",
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "ytelse": {
                    "type": "PLEIEPENGER_SYKT_BARN",
                    "barn": {
                      "norskIdentitetsnummer": "$barnIdentitetsnummer"
                    },
                    "arbeidstid": {
                      "arbeidstakerList": [
                        {
                          "organisasjonsnummer": "917755736",
                          "arbeidstidInfo": {
                            "perioder": {
                              "2021-01-07/2021-01-07": {
                                "jobberNormaltTimerPerDag": "PT1H0M",
                                "faktiskArbeidTimerPerDag": "PT0H"
                              }
                            }
                          }
                        }
                      ]
                    }
                  }
                }
            """.trimIndent()
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + ENDRINGSMELDING_URL + INNSENDING_URL,
            jwtToken = mockOAuth2Server.issueToken(fnr = søkerMedBarn),
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse =
            //language=json
            """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "ytelse.arbeidstid.arbeidstakerList[0].perioder",
                      "reason": "Perioden er utenfor gyldig interval. Gyldig interva: ([[2018-01-01, 2021-01-01]]), Ugyldig periode: 2021-01-07/2021-01-07",
                      "invalid_value": "K9-format feilkode: ugyldigPeriode"
                    }
                  ]
                }
            """.trimIndent(),
            requestEntity = endringsmelding,
            engine = engine,
            logger = logger
        )
    }

    @Test
    fun `Gitt Sammenslått søknad med flere søknadsperioder, skal endring av periode utenfor eksisterende perioder feile`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        wireMockServer.stubSifInnsynApi(
            k9SakInnsynSøknader = listOf(
                defaultK9SakInnsynSøknad(
                    barn = InnsynBarn(
                        fødselsdato = LocalDate.parse("2000-08-27"),
                        fornavn = "BARNESEN",
                        mellomnavn = "EN",
                        etternavn = "BARNESEN",
                        aktørId = "1000000000001",
                        identitetsnummer = barnIdentitetsnummer
                    ),
                    søknad = defaultK9FormatPSB(
                        søknadsPeriode = listOf(
                            Periode(LocalDate.parse("2022-12-05"), LocalDate.parse("2022-12-06")),
                            Periode(LocalDate.parse("2022-12-08"), LocalDate.parse("2022-12-09"))
                        ),
                        arbeidstid = Arbeidstid().medArbeidstaker(
                            listOf(
                                Arbeidstaker()
                                    .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                                    .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                                    .medArbeidstidInfo(
                                        ArbeidstidInfo().medPerioder(
                                            mapOf(
                                                Periode(
                                                    LocalDate.parse("2022-12-05"),
                                                    LocalDate.parse("2022-12-06")
                                                ) to ArbeidstidPeriodeInfo()
                                                    .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                    .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                                                Periode(
                                                    LocalDate.parse("2022-12-08"),
                                                    LocalDate.parse("2022-12-09")
                                                ) to ArbeidstidPeriodeInfo()
                                                    .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                    .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                                            )
                                        )
                                    )
                            )
                        )
                    )
                )
            )
        )

        //language=json
        val endringsmelding = """
                {
                  "søknadId": "$søknadId",
                  "språk": "nb",
                  "mottattDato": "$mottattDato",
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "ytelse": {
                    "type": "PLEIEPENGER_SYKT_BARN",
                    "barn": {
                      "norskIdentitetsnummer": "$barnIdentitetsnummer"
                    },
                    "arbeidstid": {
                      "arbeidstakerList": [
                        {
                          "organisasjonsnummer": "917755736",
                          "arbeidstidInfo": {
                            "perioder": {
                              "2022-12-07/2022-12-07": {
                                "jobberNormaltTimerPerDag": "PT1H0M",
                                "faktiskArbeidTimerPerDag": "PT0H"
                              }
                            }
                          }
                        }
                      ]
                    }
                  }
                }
            """.trimIndent()
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = PLEIEPENGER_SYKT_BARN_URL + ENDRINGSMELDING_URL + INNSENDING_URL,
            jwtToken = mockOAuth2Server.issueToken(fnr = søkerMedBarn),
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse =
            //language=json
            """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "ytelse.arbeidstid.arbeidstakerList[0].perioder",
                      "reason": "Perioden er utenfor gyldig interval. Gyldig interva: ([[2022-12-05, 2022-12-06], [2022-12-08, 2022-12-09]]), Ugyldig periode: 2022-12-07/2022-12-07",
                      "invalid_value": "K9-format feilkode: ugyldigPeriode"
                    }
                  ]
                }
            """.trimIndent(),
            requestEntity = endringsmelding,
            engine = engine,
            logger = logger
        )
    }

    private fun hentOgAsserEndringsmelding(forventenEndringsmelding: String, endringsmelding: JSONObject) {
        val komplettEndringsmelding = kafkaKonsumer.hentSøknad(
            endringsmelding.getString("søknadId"),
            Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN
        )

        JSONAssert.assertEquals(
            forventenEndringsmelding,
            komplettEndringsmelding.data,
            JSONCompareMode.STRICT
        )
    }
}
