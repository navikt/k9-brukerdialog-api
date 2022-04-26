package no.nav.k9brukerdialogapi.mellomlagring

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import java.time.ZoneOffset
import java.time.ZonedDateTime

class CacheRequest(
    val nøkkelPrefiks: String,
    val verdi: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val endret: ZonedDateTime? = null
) {
    companion object{
        internal fun genererCacheRequest(verdi: String, timerMellomlagret: Long, ytelse: Ytelse) = CacheRequest(
            nøkkelPrefiks = genererNøkkelPrefix(ytelse),
            verdi = verdi,
            utløpsdato = ZonedDateTime.now(ZoneOffset.UTC).plusHours(timerMellomlagret),
            opprettet = ZonedDateTime.now(ZoneOffset.UTC),
            endret = null
        )
    }
}

internal fun genererNøkkelPrefix(ytelse: Ytelse) = "mellomlagring_$ytelse"

data class CacheResponse(
    val nøkkel: String,
    val verdi: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val endret: ZonedDateTime? = null
)

class CacheConflictException(nøkkelPrefiks: String) :
    RuntimeException("Cache med nøkkelPrefiks = $nøkkelPrefiks finnes allerede for person.")

class CacheNotFoundException(nøkkelPrefiks: String) :
    RuntimeException("Cache med nøkkelPrefiks = $nøkkelPrefiks for person ble ikke funnet.")