package no.nav.k9brukerdialogapi.mellomlagring

import no.nav.helse.redis.RedisStore
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import java.util.*

class MellomlagringService(
    private val redisStore: RedisStore,
    private val passphrase: String,
    private val mellomlagretTidTimer: String
) {
    private fun genererKey(ytelse: Ytelse, fnr: String) = ytelse.toString() +"_"+ fnr

    fun getMellomlagring(
        ytelse: Ytelse,
        fnr: String
    ): String? {
        val krypto = Krypto(passphrase, fnr)
        val encrypted = redisStore.get(genererKey(ytelse, fnr)) ?: return null
        return krypto.decrypt(encrypted)
    }

    fun setMellomlagring(
        ytelse: Ytelse,
        fnr: String,
        verdi: String,
        expirationDate: Date = Calendar.getInstance().let {
            it.add(Calendar.HOUR, mellomlagretTidTimer.toInt())
            it.time
        }
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.set(genererKey(ytelse, fnr), krypto.encrypt(verdi), expirationDate)
    }

    fun updateMellomlagring(
        ytelse: Ytelse,
        fnr: String,
        verdi: String
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.update(genererKey(ytelse, fnr), krypto.encrypt(verdi))
    }

    fun deleteMellomlagring(ytelse: Ytelse, fnr: String) =
        redisStore.delete(genererKey(ytelse, fnr))


    fun getTTLInMs(ytelse: Ytelse, fnr: String): Long =
        redisStore.getPTTL(genererKey(ytelse, fnr))
}