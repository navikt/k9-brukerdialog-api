package no.nav.k9brukerdialogapi.mellomlagring

import io.ktor.server.application.*
import io.ktor.server.request.*
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import java.net.URI

internal suspend fun ApplicationCall.respondCacheConflictProblemDetails() = respondProblemDetails(
    DefaultProblemDetails(
        title = "cache-conflict",
        status = 409,
        detail = "Konflikt ved mellomlagring. Nøkkel eksisterer allerede.",
        instance = URI(request.path())
    )
)

internal suspend fun ApplicationCall.respondCacheNotFoundProblemDetails() = respondProblemDetails(
    DefaultProblemDetails(
        title = "cache-ikke-funnet",
        status = 404,
        detail = "Cache ble ikke funnet.",
        instance = URI(request.path())
    )
)