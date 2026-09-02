package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.sync.SyncOrchestrator
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Manual sync trigger, e.g. for the tg-bot to request a fresh pull on demand. */
@RestController
@RequestMapping("/api/v1/sync")
class SyncController(private val orchestrator: SyncOrchestrator) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/run")
    fun run(): Map<String, Boolean> {
        Thread({
            try {
                orchestrator.runFullSync()
            } catch (e: Exception) {
                log.error("Manually triggered sync failed", e)
            }
        }, "mtracker-manual-sync").start()
        return mapOf("started" to true)
    }
}
