package com.nikichxp.mtracker.domain

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields

object WeekKeyCalculator {

    private val ZONE = ZoneId.of("Europe/Paris")
    private const val RESET_OFFSET_HOURS = 2 * 24L + 5L

    fun currentWeekKey(): String = weekKeyFor(Instant.now())

    fun weekKeyFor(instant: Instant): String {
        val effective = instant.atZone(ZONE).minusHours(RESET_OFFSET_HOURS)
        val weekFields = WeekFields.ISO
        val year = effective.get(weekFields.weekBasedYear())
        val week = effective.get(weekFields.weekOfWeekBasedYear())
        return "%d-W%02d".format(year, week)
    }
}
