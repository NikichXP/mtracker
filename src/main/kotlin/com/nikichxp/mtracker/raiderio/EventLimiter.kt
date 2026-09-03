package com.nikichxp.mtracker.raiderio

import com.nikichxp.mtracker.config.MtrackerProperties
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds

@Service
@OptIn(ExperimentalAtomicApi::class)
class EventLimiter(
    properties: MtrackerProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val requestDelayMap = properties.sync.requestDelay
    private val defaultDelay = properties.sync.defaultDelay

    private val lastEventMap = ConcurrentMap<String, AtomicLong>()

    suspend fun <T> invoke(syncKey: String, action: suspend () -> T): T {
        val delayForRequest = requestDelayMap[syncKey] ?: defaultDelay
        val lastEvent = lastEventMap[syncKey] ?: AtomicLong(0).also {
            lastEventMap[syncKey] = it
        }
        val now = System.currentTimeMillis()

        if (now - lastEvent.load() > 1000) {
            lastEvent.store(now - 1000)
        }

        val allowedCall = lastEvent.addAndFetch(delayForRequest)
        return coroutineScope {
            val diff = allowedCall - now
            if (diff > 0) {
                delay(diff.milliseconds)
            }
            val coroutine = async { action() }
            return@coroutineScope coroutine.await()
        }
    }
}
