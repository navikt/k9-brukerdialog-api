package no.nav.k9brukerdialogapi.ytelse.omsorgsdagermelding.domene

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9brukerdialogapi.general.krever
import java.time.LocalDate
import java.time.LocalDate.parse

class Koronaoverføre(
    private val antallDagerSomSkalOverføres: Int,
    private val stengingsperiode: KoronaStengingsperiode = STENGINGSPERIODE_2021
) {
    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(stengingsperiode in gyldigePerioder, "$felt.stengingsperiode må være en av $gyldigePerioder.")
        krever(antallDagerSomSkalOverføres in gyldigDagerRange, "$felt.antallDagerSomSkalOverføres må være innenfor range $gyldigDagerRange.")
    }

    companion object{
        internal val STENGINGSPERIODE_2021 = KoronaStengingsperiode(parse("2021-01-01"), parse("2021-12-31"))
        internal val gyldigePerioder = listOf(STENGINGSPERIODE_2021)
        internal val gyldigDagerRange = (1..999)
    }
}

class KoronaStengingsperiode(
    @JsonAlias("fom") @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonAlias("tom") @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate
) {
    override fun toString(): String = "KoronaStengingsperiode(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
}