package com.nikichxp.mtracker.raiderio

/**
 * Simple thread-safe rate limiter that spaces out requests by a fixed delay.
 * Blocks the caller as needed; deliberately not RPM-based since a flat per-call
 * delay is all `RaiderIoClient` needs to stay polite to the public API.
 */
class RaiderIoRateLimiter(private val minIntervalMs: Long) {

    private var lastRequestMs: Long = 0L

    @Synchronized
    fun acquire() {
        if (minIntervalMs <= 0L) return
        val now = System.currentTimeMillis()
        val waitMs = lastRequestMs + minIntervalMs - now
        if (waitMs > 0) {
            try {
                Thread.sleep(waitMs)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        lastRequestMs = System.currentTimeMillis()
    }
}
