package no.nav.k9brukerdialogapi.mellomlagring

import com.github.fppt.jedismock.RedisServer
import no.nav.helse.redis.RedisConfig
import no.nav.helse.redis.RedisStore
import no.nav.k9brukerdialogapi.ytelse.Ytelse.OMSORGSPENGER_UTVIDET_RETT
import org.awaitility.Awaitility
import org.awaitility.Durations.ONE_SECOND
import org.junit.AfterClass
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.test.*

class MellomlagringTest {

    private companion object {
        val logger = LoggerFactory.getLogger(MellomlagringTest::class.java)

        val redisServer: RedisServer = RedisServer
            .newRedisServer()
            .apply { start() }

        val redisClient = RedisConfig.redisClient(
            redisHost = "localhost",
            redisPort = redisServer.bindPort
        )

        val redisStore = RedisStore(
            redisClient
        )

        val mellomlagringService = MellomlagringService(
            redisStore,
            "VerySecretPass",
            "1"
        )

        @AfterClass
        @JvmStatic
        fun teardown() {
            redisClient.shutdown()
            redisServer.stop()
        }
    }

    @Test
    fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring(OMSORGSPENGER_UTVIDET_RETT, "test", "søknad")
        val mellomlagring = mellomlagringService.getMellomlagring(OMSORGSPENGER_UTVIDET_RETT, "test")
        assertEquals("søknad", mellomlagring)
    }

    @Test
    fun `Oppdatering av mellomlagret verdi, skal ikke slette expiry`() {
        val key = "test"
        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 1)
            it.time
        }

        mellomlagringService.setMellomlagring(
            ytelse = OMSORGSPENGER_UTVIDET_RETT,
            fnr = key,
            verdi = "test",
            expirationDate = expirationDate
        )
        val verdi = mellomlagringService.getMellomlagring(OMSORGSPENGER_UTVIDET_RETT, key)
        assertEquals("test", verdi)
        val ttl = mellomlagringService.getTTLInMs(OMSORGSPENGER_UTVIDET_RETT, key)
        assertNotEquals(ttl, -2)
        assertNotEquals(ttl, -1)

        logger.info("PTTL=$ttl")

        mellomlagringService.updateMellomlagring(OMSORGSPENGER_UTVIDET_RETT, key, "test2")
        val oppdatertVerdi = mellomlagringService.getMellomlagring(OMSORGSPENGER_UTVIDET_RETT, key)
        assertEquals("test2", oppdatertVerdi)
        assertNotEquals(ttl, -2)
        assertNotEquals(ttl, -1)
    }

    @Test
    fun `mellomlagret verdier skal være utgått etter 500 ms`() {
        val fnr = "12345678910"
        val søknad = "test"

        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MILLISECOND, 500)
            it.time
        }
        mellomlagringService.setMellomlagring(OMSORGSPENGER_UTVIDET_RETT, fnr, søknad, expirationDate = expirationDate)
        val forventetVerdi1 = mellomlagringService.getMellomlagring(OMSORGSPENGER_UTVIDET_RETT, fnr)
        logger.info("Hentet mellomlagret verdi = {}", forventetVerdi1)
        assertEquals("test", forventetVerdi1)
        assertNotEquals(mellomlagringService.getTTLInMs(OMSORGSPENGER_UTVIDET_RETT, fnr), -2)
        assertNotEquals(mellomlagringService.getTTLInMs(OMSORGSPENGER_UTVIDET_RETT, fnr), -1)

        Awaitility.waitAtMost(ONE_SECOND).untilAsserted {
            val forventetVerdi2 = mellomlagringService.getMellomlagring(OMSORGSPENGER_UTVIDET_RETT, fnr)
            logger.info("Hentet mellomlagret verdi = {}", forventetVerdi2)
            assertNull(forventetVerdi2)
        }
    }

    @Test
    fun `verdier skal være krypterte`() {
        val fødselsnummer = "12345678910"
        mellomlagringService.setMellomlagring(OMSORGSPENGER_UTVIDET_RETT, fødselsnummer, "søknad")
        val mellomlagring = mellomlagringService.getMellomlagring(OMSORGSPENGER_UTVIDET_RETT, fødselsnummer)
        assertNotNull(redisStore.get("OMSORGSPENGER_UTVIDET_RETT_$fødselsnummer"))
        assertNotEquals(mellomlagring, redisStore.get("mellomlagring_$fødselsnummer"))
    }

}
