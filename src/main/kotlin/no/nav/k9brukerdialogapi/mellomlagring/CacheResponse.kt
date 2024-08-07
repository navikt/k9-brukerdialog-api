package no.nav.k9brukerdialogapi.mellomlagring

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.ZonedDateTime

data class CacheResponse(
    val nøkkel: String,
    val verdi: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING) val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING) val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING) val endret: ZonedDateTime? = null
)
