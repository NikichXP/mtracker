package com.nikichxp.mtracker.raiderio

import com.nikichxp.mtracker.config.MtrackerProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventLimiter(
    properties: MtrackerProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val requestDelayMap = properties.sync.requestDelay
    private val defaultDelay = properties.sync.defaultDelay

    private val mutex = Mutex()
    private var lastRequestMs: Long = 0L

    suspend fun acquire() {
        if (minIntervalMs <= 0L) return
        mutex.withLock {
            val now = System.currentTimeMillis()
            val waitMs = lastRequestMs + minIntervalMs - now
            if (waitMs > 0) {

                delay(waitMs)
            }
            lastRequestMs = System.currentTimeMillis()
        }
    }
}
