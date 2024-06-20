package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.fpsak.tidsserie.LocalDateTimeline
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9brukerdialogapi.general.erFørEllerLik
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdÅrsak as K9UtenlandsoppholdÅrsak
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
    val erUtenforEøs: Boolean?,
    val erSammenMedBarnet: Boolean?,
    val erBarnetInnlagt: Boolean?,
    val perioderBarnetErInnlagt: List<Periode> = listOf(),
    val årsak: Årsak?,
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn', erUtenforEos=$erUtenforEøs, erBarnetInnlagt=$erBarnetInnlagt, erSammenMedBarnet=$erSammenMedBarnet, årsak=$årsak)"
    }

    fun somUtenlandsoppholdPeriodeInfo() = UtenlandsoppholdPeriodeInfo()
            .medLand(Landkode.of(landkode))
            .apply {
                if (årsak != null && årsak != no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.Årsak.ANNET) {
                    medÅrsak(årsak.tilK9Årsak())
                }
            }

    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før $felt.tilOgMed")
        krever(landkode.isNotEmpty(), "$felt.landkode kan ikke være tomt")
        krever(landnavn.isNotEmpty(), "$felt.landnavn kan ikke være tomt")

        if (erBarnetInnlagt == true) {
            krever(årsak != null, "$felt.årsak må være satt når $felt.erBarnetInnlagt er true")
            krever(perioderBarnetErInnlagt.isNotEmpty(), "$felt.perioderBarnetErInnlagt kan ikke være tom når $felt.erBarnetInnlagt er true")
        }
    }
}

data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean? = null,
    val opphold: List<Utenlandsopphold> = listOf()
) {
    internal fun tilK9Utenlandsopphold(): K9Utenlandsopphold {
        val perioder = mutableMapOf<K9Periode, UtenlandsoppholdPeriodeInfo>()

        opphold.forEach { utenlandsopphold ->
            var tidslinjeUtenInnleggelse = LocalDateTimeline(utenlandsopphold.fraOgMed, utenlandsopphold.tilOgMed, 1)

            utenlandsopphold.perioderBarnetErInnlagt.forEach { periodeMedInnleggelse ->
                tidslinjeUtenInnleggelse = tidslinjeUtenInnleggelse.disjoint(periodeMedInnleggelse.somLocalDateInterval())
                perioder[K9Periode(periodeMedInnleggelse.fraOgMed, periodeMedInnleggelse.tilOgMed)] = utenlandsopphold.somUtenlandsoppholdPeriodeInfo()
            }

            val gjenværendePerioderUtenInnleggelse = tidslinjeUtenInnleggelse.toSegments().map {
                Periode(it.fom, it.tom)
            }

            gjenværendePerioderUtenInnleggelse.forEach { periodeUtenInnleggelse ->
                perioder[K9Periode(periodeUtenInnleggelse.fraOgMed, periodeUtenInnleggelse.tilOgMed)] = UtenlandsoppholdPeriodeInfo()
                    .medLand(Landkode.of(utenlandsopphold.landkode))
            }
        }

        return K9Utenlandsopphold().medPerioder(perioder)
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(skalOppholdeSegIUtlandetIPerioden, "$felt.skalOppholdeSegIUtlandetIPerioden kan ikke være null")
        opphold.forEachIndexed { index, utenlandsopphold ->
            addAll(utenlandsopphold.valider("$felt.opphold[$index]"))
        }
    }
}

enum class Årsak {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING,
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD,
    ANNET;

    fun tilK9Årsak() = when (this) {
        BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING -> K9UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
        BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD -> K9UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
        else -> null
    }
}
