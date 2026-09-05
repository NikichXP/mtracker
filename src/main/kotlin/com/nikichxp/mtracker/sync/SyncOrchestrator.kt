package com.nikichxp.mtracker.sync

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.time.measureTime

@Component
class SyncOrchestrator(
    private val playerLinkService: PlayerLinkService,
    private val playerUpdateService: PlayerUpdateService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Rebuilds the players table from the current roster/tracked players, then updates
     * every player whose update is due. Players are re-fetched individually according to
     * their own `nextUpdateAt` schedule, so a full sync does not re-fetch everyone.
     */
    suspend fun runFullSync() {
        log.info("Starting full sync: rebuilding players, then updating due players")
        val duration = measureTime {
            playerLinkService.rebuildPlayers()
            playerUpdateService.updateDuePlayers()
        }
        log.info("Full sync finished in {}", duration)
    }
}
