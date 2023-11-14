package no.nav.k9brukerdialogapi.utils

import java.time.DayOfWeek
import java.time.LocalDate

fun LocalDate.ikkeErHelg(): Boolean = dayOfWeek != DayOfWeek.SUNDAY && dayOfWeek != DayOfWeek.SATURDAY
fun LocalDate.månedStart(): LocalDate {
    return this.withDayOfMonth(1)
}

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> = datesUntil(tilOgMed.plusDays(1))
    .toList()
    .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }
