package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.utils.ukedagerTilOgMed
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFremtid.JA
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFremtid.NEI
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFremtid.USIKKER
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.Duration
import java.time.Duration.ZERO
import java.time.LocalDate

data class Omsorgstilbud(
    val svarFortid: OmsorgstilbudSvarFortid? = null,
    val svarFremtid: OmsorgstilbudSvarFremtid? = null,
    val erLiktHverUke: Boolean? = null,
    val enkeltdager: List<Enkeltdag>? = null,
    val ukedager: PlanUkedager? = null,
) {
    fun valider(felt: String) = mutableListOf<String>().apply {
        if (svarFortid != null && svarFortid == OmsorgstilbudSvarFortid.JA) {
            krever(
                ukedager != null || !enkeltdager.isNullOrEmpty(),
                "Ved $felt.svarFortid=JA kan ikke både $felt.enkeltdager og $felt.ukedager være null."
            )
        }
        if (svarFremtid != null && svarFremtid == OmsorgstilbudSvarFremtid.JA) {
            krever(
                ukedager != null || !enkeltdager.isNullOrEmpty(),
                "Ved $felt.svarFremtid=JA kan ikke både $felt.enkeltdager og $felt.ukedager være null."
            )
        }

        krever(
            enkeltdager.isNullOrEmpty() || ukedager == null,
            "Kan ikke ha både $felt.enkeltdager og $felt.ukedager satt, må velge en av de."
        )

        if (erLiktHverUke == true) {
            kreverIkkeNull(ukedager, "Hvis $felt.erLiktHverUke er true må $felt.ukedager være satt.")
            krever(enkeltdager == null, "Hvis $felt.erLiktHverUke er true må $felt.enkeldager være null.")
        }

        if (erLiktHverUke == false) {
            krever(ukedager == null, "Hvis erLiktHverUke er false kan ikke ukedager være satt.")
            kreverIkkeNull(enkeltdager, "Hvis $felt.erLiktHverUke er false kan ikke $felt.enkeltdager være null")
        }
    }


    internal fun tilK9Tilsynsordning(
        periode: Periode,
        iDag: LocalDate = LocalDate.now(),
    ): Tilsynsordning {
        val tilsynsordning = Tilsynsordning()

        if (enkeltdager == null && ukedager == null) return tilsynsordning.medNullTimer(periode)

        enkeltdager?.let { beregnEnkeltdager(tilsynsordning) }

        if (ukedager != null && svarFortid != null) beregnUkedagerFortid(tilsynsordning, periode, iDag.minusDays(1))
        if (ukedager != null && svarFremtid != null) beregnUkedagerFremtid(tilsynsordning, periode, iDag)

        return tilsynsordning
    }

    private fun beregnUkedagerFortid(tilsynsordning: Tilsynsordning, periode: Periode, iGår: LocalDate) {
        require(ukedager != null) { "Ukedager må være satt." }
        //Kan være at søknadsperiode kun er i fortiden. Derfor må vi sjekke om periode.tilOgMed er før eller etter i går.
        val gjeldendeTilOgMed = if (periode.tilOgMed.isBefore(iGår)) periode.tilOgMed else iGår
        when (svarFortid) {
            OmsorgstilbudSvarFortid.JA -> beregnFraPlanUkedager(tilsynsordning, periode.fraOgMed, gjeldendeTilOgMed)
            OmsorgstilbudSvarFortid.NEI -> tilsynsordning.medNullTimer(Periode(periode.fraOgMed, gjeldendeTilOgMed))
            null -> {
                //Hvis svarFortid er null, så er det ikke satt noe svar. Da skal vi ikke beregne noe.
            }
        }
    }

    private fun beregnUkedagerFremtid(tilsynsordning: Tilsynsordning, periode: Periode, iDag: LocalDate) {
        require(ukedager != null) { "Ukedager må være satt." }
        //Kan være at søknadsperiode kun er i fremtiden. Derfor må vi sjekke om periode.fraOgMed er før eller etter i dag.
        val gjeldendeFraOgMed = if (periode.fraOgMed.isBefore(iDag)) iDag else periode.fraOgMed
        when (svarFremtid) {
            JA -> beregnFraPlanUkedager(tilsynsordning, gjeldendeFraOgMed, periode.tilOgMed)
            NEI, USIKKER -> tilsynsordning.medNullTimer(Periode(gjeldendeFraOgMed, periode.tilOgMed))
            null -> {
                //Hvis svarFremtid er null, så er det ikke satt noe svar. Da skal vi ikke beregne noe.
            }
        }
    }

    private fun beregnEnkeltdager(tilsynsordning: Tilsynsordning) {
        require(enkeltdager != null) { "Enkeltdager må være satt" }
        enkeltdager.forEach { enkeltdag ->
            tilsynsordning.leggeTilPeriode(
                Periode(enkeltdag.dato, enkeltdag.dato),
                TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
                    ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(
                        enkeltdag.tid
                    )
                )
            )
        }
    }

    private fun beregnFraPlanUkedager(tilsynsordning: Tilsynsordning, fraOgMed: LocalDate, tilOgMed: LocalDate) {
        require(ukedager != null) { "ukedager må være satt." }
        fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { dato ->
            val tilsynslengde = ukedager.timerGittUkedag(dato.dayOfWeek)
            tilsynsordning.leggeTilPeriode(
                Periode(dato, dato),
                TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
                    ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(
                        tilsynslengde
                    )
                )
            )
        }
    }

    private fun Tilsynsordning.medNullTimer(periode: Periode) =
        leggeTilPeriode(periode, TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(ZERO))
}

enum class OmsorgstilbudSvarFortid { JA, NEI }
enum class OmsorgstilbudSvarFremtid { JA, NEI, USIKKER }

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration,
)

data class PlanUkedager(
    val mandag: Duration? = null,
    val tirsdag: Duration? = null,
    val onsdag: Duration? = null,
    val torsdag: Duration? = null,
    val fredag: Duration? = null,
) {
    companion object {
        private val NULL_ARBEIDSTIMER = ZERO
    }

    internal fun timerGittUkedag(ukedag: DayOfWeek): Duration {
        return when (ukedag) {
            MONDAY -> mandag ?: NULL_ARBEIDSTIMER
            TUESDAY -> tirsdag ?: NULL_ARBEIDSTIMER
            WEDNESDAY -> onsdag ?: NULL_ARBEIDSTIMER
            THURSDAY -> torsdag ?: NULL_ARBEIDSTIMER
            FRIDAY -> fredag ?: NULL_ARBEIDSTIMER
            SATURDAY, SUNDAY -> NULL_ARBEIDSTIMER
        }
    }
}
