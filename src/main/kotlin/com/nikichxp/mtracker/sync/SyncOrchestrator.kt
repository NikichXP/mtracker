package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.CharacterSource
import com.nikichxp.mtracker.raiderio.EventLimiter
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.time.measureTime

@Component
class SyncOrchestrator(
    private val rosterResolver: RosterResolver,
    private val characterSyncService: CharacterSyncService,
    private val playerLinkService: PlayerLinkService,
    private val props: MtrackerProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun runFullSync() {
        val roster = rosterResolver.resolve()
        val total = roster.allKeys.size
        log.info(
            "Starting full sync: {} guild members, {} friends, {} alt-only characters ({} total)",
            roster.guildMemberKeys.size, roster.friendKeys.size, roster.altKeys.size, total,
        )

        val duration = measureTime {
            syncAll(roster.guildMemberKeys, CharacterSource.GUILD)
            syncAll(roster.friendKeys, CharacterSource.FRIEND)
            syncAll(roster.altKeys, CharacterSource.ALT)
            playerLinkService.rebuildPlayers()
        }

        log.info("Full sync finished: {} characters in {}", total, duration)
    }

    private suspend fun syncAll(keys: Set<String>, source: CharacterSource) {
        for (key in keys) {
            characterSyncService.syncCharacter(key, source)
        }
    }
}
