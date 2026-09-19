package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.character.CharacterSource
import com.nikichxp.mtracker.domain.player.Player
import com.nikichxp.mtracker.domain.player.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * Updates players one by one, driven by [Player.nextUpdateAt]: each update fetches the
 * Raider.io profile of every character of the player (including the ~10 latest runs),
 * stores the runs via [RunSyncService], and then schedules the next update via
 * [UpdateScheduleService] based on the player's best Mythic+ score.
 */
@Component
class PlayerUpdateService(
    private val characterSyncService: CharacterSyncService,
    private val runSyncService: RunSyncService,
    private val updateScheduleService: UpdateScheduleService,
    private val playerRepository: PlayerRepository,
    private val props: MtrackerProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Processes every player whose update is due, oldest first, until nobody is due anymore. */
    suspend fun updateDuePlayers() {
        while (true) {
            val due = playerRepository.findDueForUpdate(Instant.now(), PageRequest.of(0, props.sync.batchSize))
            if (due.isEmpty()) return
            for (player in due) {
                updatePlayerSafely(player)
            }
        }
    }

    private suspend fun updatePlayerSafely(player: Player) {
        try {
            updatePlayer(player)
        } catch (e: Exception) {
            log.error("Failed to update player {}", player.playerKey, e)
            // Push the retry into the future so one broken player cannot hot-loop the queue.
            player.nextUpdateAt = Instant.now().plus(RETRY_DELAY)
            playerRepository.save(player)
        }
    }

    suspend fun updatePlayer(player: Player) {
        log.debug("Updating player {}", player.playerKey)
        var bestScore: Double? = null
        for (characterKey in player.characterKeys) {
            val profile = characterSyncService.syncCharacter(characterKey, inferSource(player)) ?: continue
            runSyncService.syncRecentRuns(profile.mythicPlusRecentRuns)
            val score = profile.mythicPlusScoresBySeason.firstOrNull()?.scores?.all
            if (score != null && (bestScore == null || score > bestScore)) {
                bestScore = score
            }
        }
        // Keep the previously known score when every fetch failed, so scheduling stays stable.
        val effectiveScore = bestScore ?: player.rioScore
        player.rioScore = effectiveScore
        player.lastSyncedAt = Instant.now()
        player.nextUpdateAt = updateScheduleService.nextUpdateAt(effectiveScore)
        playerRepository.save(player)
    }

    private fun inferSource(player: Player): CharacterSource = when {
        player.isGuildMember -> CharacterSource.GUILD
        player.isFriend -> CharacterSource.FRIEND
        else -> CharacterSource.ALT
    }

    companion object {
        private val RETRY_DELAY: Duration = Duration.ofMinutes(10)
    }
}
