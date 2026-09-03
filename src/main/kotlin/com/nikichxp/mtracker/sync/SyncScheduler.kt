package com.nikichxp.mtracker.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncScheduler(private val orchestrator: SyncOrchestrator) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        scope.launch {
            try {
                orchestrator.runFullSync()
            } catch (e: Exception) {
                log.error("Startup sync failed", e)
            }
        }
    }

    @Scheduled(
        fixedDelayString = "#{\${mtracker.sync.interval-hours} * 3600000}",
        initialDelayString = "#{\${mtracker.sync.interval-hours} * 3600000}",
    )
    fun onSchedule() {
        scope.launch {
            try {
                orchestrator.runFullSync()
            } catch (e: Exception) {
                log.error("Scheduled sync failed", e)
            }
        }
    }
}
