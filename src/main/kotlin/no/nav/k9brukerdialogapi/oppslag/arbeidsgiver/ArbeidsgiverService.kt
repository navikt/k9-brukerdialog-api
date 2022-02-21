package no.nav.k9brukerdialogapi.oppslag.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9brukerdialogapi.general.CallId
import no.nav.k9brukerdialogapi.oppslag.TilgangNektetException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ArbeidsgiverService(
    private val arbeidsgivereGateway: ArbeidsgiverGateway
) {
    private val logger: Logger = LoggerFactory.getLogger(ArbeidsgiverService::class.java)
    private val frilansoppdragAttributter = listOf("frilansoppdrag[]")
    private val arbeidsgivereAttributter = listOf(
        "arbeidsgivere[].organisasjoner[].organisasjonsnummer",
        "arbeidsgivere[].organisasjoner[].navn",
        "arbeidsgivere[].organisasjoner[].ansettelsesperiode"
    )
    private val privateArbeidsgivereAttributter = listOf(
        "private_arbeidsgivere[].ansettelsesperiode",
        "private_arbeidsgivere[].offentlig_ident"
    )

    suspend fun hentArbedisgivere(
        idToken: IdToken,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean
    ): Arbeidsgivere {
        var attributter = Pair("a", mutableListOf<String>())
        attributter.second.addAll(arbeidsgivereAttributter)
        if (skalHentePrivateArbeidsgivere) attributter.second.addAll(privateArbeidsgivereAttributter)
        if (skalHenteFrilansoppdrag) attributter.second.addAll(frilansoppdragAttributter)

        return try {
            arbeidsgivereGateway.hentArbeidsgivere(
                idToken,
                callId,
                listOf(
                    attributter,
                    Pair("fom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))),
                    Pair("tom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed)))
                )
            )
        } catch (cause: Throwable) {
            when (cause) {
                is TilgangNektetException -> throw cause
                else -> {
                    logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
                    Arbeidsgivere(emptyList(), emptyList(), emptyList())
                }
            }
        }
    }
}