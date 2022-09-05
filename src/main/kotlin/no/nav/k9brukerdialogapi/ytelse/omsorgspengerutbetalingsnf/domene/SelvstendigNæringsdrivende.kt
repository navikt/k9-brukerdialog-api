package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogapi.general.erLikEllerEtter
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.general.kreverIkkeNull
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Regnskapsfører.Companion.leggTilK9Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.VarigEndring.Companion.leggTilVarigEndring
import java.math.BigDecimal
import java.time.LocalDate
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende as K9SelvstendigNæringsdrivende

class SelvstendigNæringsdrivende(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate? = null,
    private val næringstype: Næringstype,
    private val fiskerErPåBladB: Boolean? = null,
    private val næringsinntekt: Int? = null,
    private val navnPåVirksomheten: String,
    private val organisasjonsnummer: String? = null,
    private val registrertINorge: Boolean? = null,
    private val registrertIUtlandet: Land? = null,
    private val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    private val varigEndring: VarigEndring? = null,
    private val regnskapsfører: Regnskapsfører? = null,
    private val erNyoppstartet: Boolean,
    private val harFlereAktiveVirksomheter: Boolean? = null
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(harFlereAktiveVirksomheter, "$felt.harFlereAktiveVirksomheter kan ikke være null.")
        validerErNyoppstartet(felt)
        tilOgMed?.let { krever(it.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være før eller lik tilOgMed.") }
        registrertIUtlandet?.let { addAll(it.valider("$felt.registrertIUtlandet")) }
        organisasjonsnummer?.let {
            krever(it.all { tall -> tall.isDigit() }, "$felt.organisasjonsnummer kan kun bestå av tall.")
        }
    }

    private fun MutableList<String>.validerErNyoppstartet(felt: String) {
        val fireÅrSiden = LocalDate.now().minusYears(4)
        if(erNyoppstartet) krever(fraOgMed.erLikEllerEtter(fireÅrSiden), "$felt.fraOgMed Dersom nyOppstartet er true må fraOgMed være maks 4 år siden.")
        if(!erNyoppstartet) krever(fraOgMed.isBefore(fireÅrSiden), "$felt.fraOgMed Dersom nyOppstartet er false må fraOgMed være over 4 år siden.")
    }

    fun somK9SelvstendigNæringsdrivende() = K9SelvstendigNæringsdrivende().apply {
        medVirksomhetNavn(navnPåVirksomheten)
        medPerioder(mapOf(Periode(fraOgMed, tilOgMed) to byggK9SelvstendingNæringsdrivendeInfo()))
        this@SelvstendigNæringsdrivende.organisasjonsnummer?.let { medOrganisasjonsnummer(Organisasjonsnummer.of(it)) }
    }

    private fun byggK9SelvstendingNæringsdrivendeInfo()= SelvstendigNæringsdrivendePeriodeInfo().apply {
        medVirksomhetstyper(listOf(næringstype.somK9Virksomhetstype()))
        medRegistrertIUtlandet(!registrertINorge!!)
        medErNyoppstartet(this@SelvstendigNæringsdrivende.erNyoppstartet)

        næringsinntekt?.let { medBruttoInntekt(BigDecimal.valueOf(it.toLong())) }
        regnskapsfører?.let { leggTilK9Regnskapsfører(it) }
        yrkesaktivSisteTreFerdigliknedeÅrene?.let { medErNyIArbeidslivet(true) }
        varigEndring?.let { leggTilVarigEndring(it) }

        this@SelvstendigNæringsdrivende.registrertIUtlandet?.let {
            medLandkode(it.somK9Landkode())
        } ?: medLandkode(Landkode.NORGE)
    }
}

class YrkesaktivSisteTreFerdigliknedeArene(
    private val oppstartsdato: LocalDate
)