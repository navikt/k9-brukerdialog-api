package no.nav.k9brukerdialogapi

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.k9brukerdialogapi.ytelse.omsorgspengermidlertidigalene.domene.OmsorgspengerMdlertidigAleneKomplettSøknad
import org.json.JSONObject

val objectMapper: ObjectMapper = jacksonObjectMapper().dusseldorfConfigured()
    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)

fun ObjectMapper.k9MellomlagringKonfigurert(): ObjectMapper {
    return jacksonObjectMapper().dusseldorfConfigured().apply {
        configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }
}

fun ObjectMapper.k9BrukerdialogCacheKonfigurert(): ObjectMapper {
    return dusseldorfConfigured().apply {
        configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    }
}

fun Any.somJson() = objectMapper.writeValueAsString(this)
fun JSONObject.somOmsorgspengerMidlertidigAleneKomplettSøknad(): OmsorgspengerMdlertidigAleneKomplettSøknad = objectMapper.readValue(this.toString())
fun JSONObject.somOmsorgspengerUtvidetRettKomplettSøknad(): no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnKomplettSøknad = objectMapper.readValue(this.toString())
fun JSONObject.somEttersendingKomplettSøknad(): no.nav.k9brukerdialogapi.ytelse.ettersending.domene.KomplettEttersendelse = objectMapper.readValue(this.toString())
fun JSONObject.somOmsorgsdagerAleneomsorgKomplettSøknad(): no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene.OmsorgsdagerAleneOmOmsorgenKomplettSøknad = objectMapper.readValue(this.toString())
fun JSONObject.somOmsorgspengerUtbetalingArbeidstakerKomplettSøknad(): no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingarbeidstaker.domene.OmsorgspengerutbetalingArbeidstakerKomplettSøknad = objectMapper.readValue(this.toString())
fun JSONObject.somOmsorgspengerUtbetalingSnfKomplettSøknad(): no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene.OmsorgspengerutbetalingSnfKomplettSøknad = objectMapper.readValue(this.toString())
fun JSONObject.somPleiepengerLivetsSluttfaseKomplettSøknad(): no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PilsKomplettSøknad = objectMapper.readValue(this.toString())
