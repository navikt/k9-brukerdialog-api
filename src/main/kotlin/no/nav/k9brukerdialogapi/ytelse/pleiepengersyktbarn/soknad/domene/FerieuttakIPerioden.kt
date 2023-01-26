package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.ytelse.psb.v1.LovbestemtFerie
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import java.time.LocalDate
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class FerieuttakIPerioden(
    val skalTaUtFerieIPerioden: Boolean,
    val ferieuttak: List<Ferieuttak>,
) {
    override fun toString(): String {
        return "FerieuttakIPerioden(skalTaUtFerieIPerioden=$skalTaUtFerieIPerioden, ferieuttak=$ferieuttak)"
    }

    fun tilK9LovbestemtFerie(): LovbestemtFerie {
        if (!skalTaUtFerieIPerioden) return LovbestemtFerie()

        val perioder = mutableMapOf<K9Periode, LovbestemtFerie.LovbestemtFeriePeriodeInfo>()

        ferieuttak.forEach { ferieuttak ->
            perioder[K9Periode(ferieuttak.fraOgMed, ferieuttak.tilOgMed)] = LovbestemtFerie.LovbestemtFeriePeriodeInfo()
        }

        return LovbestemtFerie().medPerioder(perioder)
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        ferieuttak.forEachIndexed { index, ferieuttak ->
            addAll(ferieuttak.valider("ferieuttak[$index]"))
        }
    }
}

data class Ferieuttak(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
) {
    override fun toString(): String {
        return "Ferieuttak(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før $felt.tilOgMed")
    }
}
