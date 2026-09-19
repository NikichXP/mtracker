package com.nikichxp.mtracker.topgear

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Component
@ConditionalOnProperty(name = ["mtracker.top-gear.scheduler-enabled"], havingValue = "true", matchIfMissing = true)
class TopGearScheduler(
    private val producer: TopPlayerScanTaskProducer,
    private val scanService: TopGearScanService,
    private val cleanupService: TopGearCleanupService,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val producerRunning = AtomicBoolean(false)
    private val scanRunning = AtomicBoolean(false)
    private val cleanupRunning = AtomicBoolean(false)

    @Scheduled(
        fixedDelayString = "\${mtracker.top-gear.producer-interval-hours:24}",
        initialDelayString = "\${mtracker.top-gear.producer-interval-hours:24}",
        timeUnit = TimeUnit.HOURS,
    )
    fun produceTasks() {
        if (!producerRunning.compareAndSet(false, true)) return
        scope.launch {
            try {
                producer.produceTasks()
            } catch (e: Exception) {
                log.error("Scheduled top-player task production failed", e)
            } finally {
                producerRunning.set(false)
            }
        }
    }

    @Scheduled(
        fixedDelayString = "\${mtracker.top-gear.scan-poll-interval-ms:30000}",
        initialDelayString = "\${mtracker.top-gear.scan-poll-interval-ms:30000}",
    )
    fun scanTasks() {
        if (!scanRunning.compareAndSet(false, true)) return
        scope.launch {
            try {
                scanService.scanDueTasks()
            } catch (e: Exception) {
                log.error("Scheduled top-gear scan failed", e)
            } finally {
                scanRunning.set(false)
            }
        }
    }

    @Scheduled(
        fixedDelayString = "\${mtracker.top-gear.cleanup-interval-hours:1}",
        initialDelayString = "\${mtracker.top-gear.cleanup-interval-hours:1}",
        timeUnit = TimeUnit.HOURS,
    )
    fun cleanup() {
        if (!cleanupRunning.compareAndSet(false, true)) return
        scope.launch {
            try {
                cleanupService.purgeDeprecated()
            } catch (e: Exception) {
                log.error("Scheduled top-gear cleanup failed", e)
            } finally {
                cleanupRunning.set(false)
            }
        }
    }
}
