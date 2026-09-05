package com.nikichxp.mtracker.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class UpdateScheduleServiceTest {

    private val service = UpdateScheduleService()
    private val now = Instant.parse("2026-09-05T12:00:00Z")

    @Test
    fun `null score updates once a day`() {
        assertThat(service.nextUpdateAt(null, now)).isEqualTo(now.plus(Duration.ofDays(1)))
    }

    @Test
    fun `score below 500 updates once a day`() {
        assertThat(service.nextUpdateAt(0.0, now)).isEqualTo(now.plus(Duration.ofDays(1)))
        assertThat(service.nextUpdateAt(499.9, now)).isEqualTo(now.plus(Duration.ofDays(1)))
    }

    @Test
    fun `score between 500 and 2000 updates every 8 hours`() {
        assertThat(service.nextUpdateAt(500.0, now)).isEqualTo(now.plus(Duration.ofHours(8)))
        assertThat(service.nextUpdateAt(2000.0, now)).isEqualTo(now.plus(Duration.ofHours(8)))
    }

    @Test
    fun `score above 2000 updates every hour`() {
        assertThat(service.nextUpdateAt(2000.1, now)).isEqualTo(now.plus(Duration.ofHours(1)))
        assertThat(service.nextUpdateAt(3500.0, now)).isEqualTo(now.plus(Duration.ofHours(1)))
    }
}
