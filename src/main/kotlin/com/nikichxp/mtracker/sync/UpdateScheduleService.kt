package com.nikichxp.mtracker.sync

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * Decides when a player should be updated next, based on their Mythic+ score:
 * - rio < 500       -> once a day
 * - rio 500..2000   -> every 8 hours
 * - rio > 2000      -> every hour
 */
@Component
class UpdateScheduleService {

    fun nextUpdateAt(rioScore: Double?, now: Instant = Instant.now()): Instant =
        now.plus(updateInterval(rioScore))

    fun updateInterval(rioScore: Double?): Duration {
        val score = rioScore ?: 0.0
        return when {
            score > HIGH_RIO_THRESHOLD -> Duration.ofHours(1)
            score >= LOW_RIO_THRESHOLD -> Duration.ofHours(8)
            else -> Duration.ofDays(1)
        }
    }

    companion object {
        const val LOW_RIO_THRESHOLD = 500.0
        const val HIGH_RIO_THRESHOLD = 2000.0
    }
}
