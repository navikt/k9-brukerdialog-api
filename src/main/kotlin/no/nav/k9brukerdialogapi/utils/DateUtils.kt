package no.nav.k9brukerdialogapi.utils

import java.time.DayOfWeek
import java.time.LocalDate

fun LocalDate.ikkeErHelg(): Boolean = dayOfWeek != DayOfWeek.SUNDAY && dayOfWeek != DayOfWeek.SATURDAY
fun LocalDate.månedStart(): LocalDate {
    return this.withDayOfMonth(1)
}
