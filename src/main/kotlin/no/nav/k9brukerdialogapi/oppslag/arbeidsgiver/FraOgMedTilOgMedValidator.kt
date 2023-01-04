package no.nav.k9brukerdialogapi.oppslag.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

class FraOgMedTilOgMedValidator {

    companion object {
        fun valider(
            fraOgMed: String?,
            tilOgMed: String?,
        ): Set<Violation> {
            val feil = mutableSetOf<Violation>()
            val parsedFraOgMed: LocalDate? = feil.parseDato(fraOgMed, "fra_og_med")
            val parsedTilOgMed: LocalDate? = feil.parseDato(tilOgMed, "til_og_med")

            if (parsedFraOgMed == null) {
                feil.add(
                    Violation(
                        parameterName = "fra_og_med",
                        parameterType = ParameterType.QUERY,
                        reason = "fra_og_med kan ikke være null",
                        invalidValue = fraOgMed
                    )
                )
            }

            if (parsedTilOgMed == null) {
                feil.add(
                    Violation(
                        parameterName = "til_og_med",
                        parameterType = ParameterType.QUERY,
                        reason = "til_og_med kan ikke være null",
                        invalidValue = tilOgMed
                    )
                )
            }

            if (parsedTilOgMed != null && parsedFraOgMed != null && parsedTilOgMed.isBefore(parsedFraOgMed)) {
                feil.add(
                    Violation(
                        parameterName = "fra_og_med og til_og_med",
                        parameterType = ParameterType.QUERY,
                        reason = "til_og_med kan ikke være før fra_og_med",
                        invalidValue = "fra_og_med=$parsedFraOgMed og til_og_med=$parsedTilOgMed"
                    )
                )
            }

            return feil
        }

        private fun MutableSet<Violation>.parseDato(dato: String?, navn: String): LocalDate? {
            try {
                return LocalDate.parse(dato)
            } catch (exception: Exception) {
                add(
                    Violation(
                        parameterName = "$navn",
                        parameterType = ParameterType.QUERY,
                        reason = "$navn er på ugyldig format. Feil: $exception",
                        invalidValue = dato
                    )
                )
            }
            return null
        }
    }
}
