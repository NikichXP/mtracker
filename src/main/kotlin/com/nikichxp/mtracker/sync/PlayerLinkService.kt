package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.domain.Player
import com.nikichxp.mtracker.domain.PlayerRepository
import com.nikichxp.mtracker.domain.TrackedPlayerRepository
import org.springframework.stereotype.Component

/**
 * Groups characters into [Player]s (main + alts).
 *
 * IMPORTANT LIMITATION: Raider.io's public API has no field linking a character to its
 * alts, so this is entirely driven by [com.nikichxp.mtracker.domain.TrackedPlayer] entries
 * (managed via [com.nikichxp.mtracker.web.TrackedPlayerAdminController]). Any character not
 * explicitly listed there is treated as its own standalone main. This is not a bug - it's a
 * hard limit of the upstream API - so don't expect automatic alt discovery here.
 */
@Component
class PlayerLinkService(
    private val rosterResolver: RosterResolver,
    private val playerRepository: PlayerRepository,
    private val trackedPlayerRepository: TrackedPlayerRepository,
) {

    fun rebuildPlayers() {
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
            ),
        )
    }
}
