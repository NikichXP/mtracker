package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.service.JobRunLockService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

@Component
@ConditionalOnProperty(name = ["mtracker.top-gear.scheduler-enabled"], havingValue = "true", matchIfMissing = true)
class TopGearScheduler(
    private val producer: TopPlayerScanTaskProducer,
    private val scanService: TopGearScanService,
    private val cleanupService: TopGearCleanupService,
    private val jobRunLockService: JobRunLockService,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val producerRunning = AtomicBoolean(false)
    private val scanRunning = AtomicBoolean(false)
    private val cleanupRunning = AtomicBoolean(false)

    private companion object {
        const val JOB_NAME = "top-player-scan-task-producer"
        val HEARTBEAT_INTERVAL = 60.seconds
    }

    @PostConstruct
    fun startTasksOnBoot() {
        produceTasks()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun produceTasks() {
        if (!producerRunning.compareAndSet(false, true)) return
        val lock = jobRunLockService.tryAcquire(
            JOB_NAME,
            LocalDate.now(),
            Duration.ofMinutes(props.topGear.lockStaleMinutes),
        )
        if (lock == null) {
            producerRunning.set(false)
            log.info("Top-player task production for today already claimed by another worker")
            return
        }
        scope.launch {
            val job = launch {
                try {
                    producer.produceTasks()
                } catch (e: Exception) {
                    log.error("Scheduled top-player task production failed", e)
                }
            }
            try {
                while (job.isActive) {
                    delay(HEARTBEAT_INTERVAL)
                    if (!jobRunLockService.tryHeartbeat(lock)) {
                        log.error("Lost job lock {} for today, cancelling task production", JOB_NAME)
                        job.cancel()
                        break
                    }
                }
            } finally {
                withContext(NonCancellable) {
                    job.join()
                    jobRunLockService.releaseIfOwned(lock)
                }
                producerRunning.set(false)
            }
        }
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
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
