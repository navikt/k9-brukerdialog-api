package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Næringstype
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.Regnskapsfører
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.VarigEndring
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar.NEI
import java.time.LocalDate
import kotlin.test.Test

class SelvstendigNæringsdrivendeTest {

    @Test
    fun `Valideringsfeil`(){
        SelvstendigNæringsdrivende(
            virksomhet = no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.SelvstendigNæringsdrivende(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = Næringstype.DAGMAMMA,
                næringsinntekt = 3_000_000,
                navnPåVirksomheten = "Kiwi ASA",
                organisasjonsnummer = "975959171",
                registrertINorge = true,
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                    oppstartsdato = LocalDate.parse("2022-01-01")
                ),
                varigEndring = VarigEndring(
                    dato = LocalDate.parse("2022-01-01"),
                    inntektEtterEndring = 1_500_00,
                    forklaring = "Fordi atte atte atte"
                ),
                regnskapsfører = Regnskapsfører(
                    navn = "Knut",
                    telefon = "123123123"
                ),
                erNyoppstartet = true,
                harFlereAktiveVirksomheter = true
            ),
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(NEI, emptyList()))
        )
    }
}