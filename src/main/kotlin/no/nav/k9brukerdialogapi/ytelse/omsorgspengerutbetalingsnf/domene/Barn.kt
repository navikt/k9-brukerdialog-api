package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import java.time.LocalDate

class Barn(
    private val navn: String,
    private val fødselsdato: LocalDate,
    private val type: TypeBarn,
    private val aktørId: String? = null,
    private val utvidetRett: Boolean? = null,
    private var identitetsnummer: String? = null
) {
    fun valider(felt: String) = mutableListOf<String>().apply {

    }
}

enum class TypeBarn{
    FOSTERBARN,
    ANNET,
    FRA_OPPSLAG
}