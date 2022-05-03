package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.ytelse.Ytelse
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Søknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutbetalingarbeidstaker.omsorgspengerUtbetalingArbeidstakerApi.kt")

fun Route.omsorgspengerUtbetalingArbeidstakerApi(
    omsorgspengerUtbetalingArbeidstakerService: OmsorgspengerUtbetalingArbeidstakerService
) {
    route(OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<Søknad>()
            logger.info(formaterStatuslogging(Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER, søknad.søknadId, "mottatt."))

            omsorgspengerUtbetalingArbeidstakerService.registrer(søknad, )

            registrerMottattSøknad(Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}