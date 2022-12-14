package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.LocalDate
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean? = null,
    val utenlandsoppholdSiste12Mnd: List<Bosted> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean? = null,
    val utenlandsoppholdNeste12Mnd: List<Bosted> = listOf(),
) {
    fun tilK9Bosteder(): Bosteder? {
        val perioder = mutableMapOf<no.nav.k9.søknad.felles.type.Periode, Bosteder.BostedPeriodeInfo>()

        utenlandsoppholdSiste12Mnd.forEach { bosted ->
            if (bosted.landkode.isNotEmpty()) perioder[K9Periode(bosted.fraOgMed, bosted.tilOgMed)] =
                Bosteder.BostedPeriodeInfo()
                    .medLand(Landkode.of(bosted.landkode))
        }

        utenlandsoppholdNeste12Mnd.forEach { bosted ->
            if (!bosted.landkode.isNullOrEmpty()) perioder[K9Periode(bosted.fraOgMed, bosted.tilOgMed)] =
                Bosteder.BostedPeriodeInfo()
                    .medLand(Landkode.of(bosted.landkode))
        }

        return Bosteder().medPerioder(perioder)
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(harBoddIUtlandetSiste12Mnd, "$felt.harBoddIUtlandetSiste12Mnd kan ikke være null")
        utenlandsoppholdSiste12Mnd.forEachIndexed { index, bosted ->
            addAll(bosted.valider("$felt.utenlandsoppholdSiste12Mnd[$index]"))
        }

        kreverIkkeNull(skalBoIUtlandetNeste12Mnd, "$felt.skalBoIUtlandetNeste12Mnd kan ikke være null")
        utenlandsoppholdNeste12Mnd.forEachIndexed { index, bosted ->
            addAll(bosted.valider("$felt.utenlandsoppholdNeste12Mnd[$index]"))
        }
    }
}

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn')"
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før $felt.tilOgMed")
        krever(landkode.isNotEmpty(), "$felt.landkode kan ikke være tomt")
        krever(landnavn.isNotEmpty(), "$felt.landnavn kan ikke være tomt")
    }
}
