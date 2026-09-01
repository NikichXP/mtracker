package com.nikichxp.mtracker.domain

import org.assertj.core.api.Assertions.assertThat
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

class WeekKeyCalculatorTest {

    private val zone = ZoneId.of("Europe/Paris")

    @Test
    fun `moments before the wednesday reset belong to the previous week key`() {
        val beforeReset = ZonedDateTime.of(2024, 1, 3, 4, 59, 0, 0, zone).toInstant()
        val afterReset = ZonedDateTime.of(2024, 1, 3, 5, 0, 0, 0, zone).toInstant()
        assertThat(WeekKeyCalculator.weekKeyFor(beforeReset))
            .isNotEqualTo(WeekKeyCalculator.weekKeyFor(afterReset))
    }

    @Test
    fun `moments within the same reset window share a week key`() {
        val justAfterReset = ZonedDateTime.of(2024, 1, 3, 6, 0, 0, 0, zone).toInstant()
        val laterSameWeek = ZonedDateTime.of(2024, 1, 9, 4, 0, 0, 0, zone).toInstant()
        assertThat(WeekKeyCalculator.weekKeyFor(justAfterReset))
            .isEqualTo(WeekKeyCalculator.weekKeyFor(laterSameWeek))
    }
}
