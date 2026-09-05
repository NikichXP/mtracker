package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.domain.Player
import com.nikichxp.mtracker.domain.PlayerRepository
import com.nikichxp.mtracker.domain.TrackedPlayerRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PlayerLinkService(
    private val rosterResolver: RosterResolver,
    private val playerRepository: PlayerRepository,
    private val trackedPlayerRepository: TrackedPlayerRepository,
) {

    suspend fun rebuildPlayers() {
        val roster = rosterResolver.resolve()
        val linkedKeys = mutableSetOf<String>()

        for (trackedPlayer in trackedPlayerRepository.findAll()) {
            val characterKeys = trackedPlayer.characterKeys.map { it.trim() }.filter { it.isNotBlank() }
            if (characterKeys.isEmpty()) continue
            val main = characterKeys.first()
            linkedKeys += characterKeys
            upsertPlayer(
                playerKey = main,
                displayName = trackedPlayer.displayName,
                characterKeys = characterKeys,
                isFriend = trackedPlayer.isFriend,
                isGuildMember = characterKeys.any { it in roster.guildMemberKeys },
            )
        }

        for (characterKey in roster.allKeys) {
            if (characterKey in linkedKeys) continue
            upsertPlayer(
                playerKey = characterKey,
                displayName = characterKey,
                characterKeys = listOf(characterKey),
                isFriend = characterKey in roster.friendKeys,
                isGuildMember = characterKey in roster.guildMemberKeys,
            )
        }
    }

    private fun upsertPlayer(
        playerKey: String,
        displayName: String,
        characterKeys: List<String>,
        isFriend: Boolean,
        isGuildMember: Boolean,
    ) {
        val existing = playerRepository.findByPlayerKey(playerKey)
        playerRepository.save(
            Player(
                id = existing?.id,
                playerKey = playerKey,
                displayName = displayName,
                characterKeys = characterKeys,
                isFriend = isFriend,
                isGuildMember = isGuildMember,
                rioScore = existing?.rioScore,
                lastSyncedAt = existing?.lastSyncedAt,
                nextUpdateAt = existing?.nextUpdateAt,
            ),
        )
    }
}
