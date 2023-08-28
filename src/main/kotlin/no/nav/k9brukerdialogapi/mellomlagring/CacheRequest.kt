package no.nav.k9brukerdialogapi.mellomlagring

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import java.time.ZonedDateTime

data class CacheRequest(
    val nøkkelPrefiks: String,
    val verdi: String,
    val ytelse: Ytelse? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val endret: ZonedDateTime? = null
)

class CacheConflictException(nøkkelPrefiks: String) :
    RuntimeException("Cache med nøkkelPrefiks = $nøkkelPrefiks finnes allerede for person.")

class CacheNotFoundException(nøkkelPrefiks: String) :
    RuntimeException("Cache med nøkkelPrefiks = $nøkkelPrefiks for person ble ikke funnet.")
