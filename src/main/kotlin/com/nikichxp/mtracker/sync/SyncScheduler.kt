package com.nikichxp.mtracker.sync

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncScheduler(private val orchestrator: SyncOrchestrator) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        Thread({
            try {
                orchestrator.runFullSync()
            } catch (e: Exception) {
                log.error("Startup sync failed", e)
            }
        }, "mtracker-startup-sync").start()
    }

    @Scheduled(
        fixedDelayString = "#{\${mtracker.sync.interval-hours} * 3600000}",
        initialDelayString = "#{\${mtracker.sync.interval-hours} * 3600000}",
    )
    fun onSchedule() {
        try {
            orchestrator.runFullSync()
        } catch (e: Exception) {
            log.error("Scheduled sync failed", e)
        }
    }
}
