package com.nikichxp.mtracker.raiderio

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
