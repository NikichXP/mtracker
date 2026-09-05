package com.nikichxp.mtracker.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

/** Frequently drains the queue of players whose [com.nikichxp.mtracker.domain.Player.nextUpdateAt] is due. */
@Component
class PlayerUpdateScheduler(private val playerUpdateService: PlayerUpdateService) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val running = AtomicBoolean(false)

    @Scheduled(
        fixedDelayString = "\${mtracker.sync.poll-interval-ms:60000}",
        initialDelayString = "\${mtracker.sync.poll-interval-ms:60000}",
    )
    fun onSchedule() {
        if (!running.compareAndSet(false, true)) return
        scope.launch {
            try {
                playerUpdateService.updateDuePlayers()
            } catch (e: Exception) {
                log.error("Scheduled player update failed", e)
            } finally {
                running.set(false)
            }
        }
    }
}
