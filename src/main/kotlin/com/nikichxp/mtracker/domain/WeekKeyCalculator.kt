package com.nikichxp.mtracker.domain

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields

/**
 * Computes the EU realm weekly-reset partition key ("{isoYear}-W{isoWeek}") that all
 * [WeeklySnapshot]s are bucketed by.
 *
 * The EU weekly reset happens Wednesdays ~05:00 server time (Europe/Paris, CET/CEST), which
 * is 2 days + 5 hours after the ISO week's Monday-00:00 boundary. Subtracting that fixed
 * offset before reading the ISO week aligns the reset instant with the ISO week rollover,
 * so everything from one reset up to (but excluding) the next reset shares a week key. This
 * is a pragmatic approximation: the exact reset hour may drift slightly around DST changes;
 * tune [RESET_OFFSET_HOURS] here if that ever matters, callers don't need to change.
 */
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
