package no.nav.k9brukerdialogapi.oppslag.arbeidsgiver

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.k9brukerdialogapi.ARBEIDSGIVER_URL
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.oppslag.TilgangNektetException
import no.nav.k9brukerdialogapi.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory
import java.time.LocalDate

const val fraOgMedQueryName = "fra_og_med"
const val tilOgMedQueryName = "til_og_med"
const val frilansoppdragQueryName = "frilansoppdrag"
const val privateArbeidsgivereQueryName = "private_arbeidsgivere"

private val logger = LoggerFactory.getLogger("no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.arbeidsgiverApis")

fun Route.arbeidsgiverApis(
    arbeidsgivereService: ArbeidsgiverService,
    idTokenProvider: IdTokenProvider
) {
    get(ARBEIDSGIVER_URL) {
        val fraOgMed = call.request.queryParameters[fraOgMedQueryName]
        val tilOgMed = call.request.queryParameters[tilOgMedQueryName]

        val valideringsfeil = FraOgMedTilOgMedValidator.valider(fraOgMed, tilOgMed)
        if(valideringsfeil.isNotEmpty()) throw Throwblem(ValidationProblemDetails(valideringsfeil))

        try {
            val arbeidsgivere = arbeidsgivereService.hentArbedisgivere(
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId(),
                fraOgMed = LocalDate.parse(fraOgMed),
                tilOgMed = LocalDate.parse(tilOgMed),
                skalHentePrivateArbeidsgivere = call.request.queryParameters[privateArbeidsgivereQueryName].toBoolean(),
                skalHenteFrilansoppdrag  = call.request.queryParameters[frilansoppdragQueryName].toBoolean()
            )

            call.respond(arbeidsgivere)
        } catch (exception: Exception){
            when (exception) {
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, exception)
                else -> throw exception
            }
        }

    }

}
