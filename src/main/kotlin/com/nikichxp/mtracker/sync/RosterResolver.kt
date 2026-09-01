package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.domain.TrackedGuildRepository
import com.nikichxp.mtracker.domain.TrackedPlayerRepository
import com.nikichxp.mtracker.raiderio.RaiderIoClient
import org.springframework.stereotype.Component

/** Resolved set of tracked characters, tagged with why they're tracked. */
data class ResolvedRoster(
    val guildMemberKeys: Set<String>,
    val friendKeys: Set<String>,
    val altKeys: Set<String>,
) {
    val allKeys: Set<String> get() = guildMemberKeys + friendKeys + altKeys
}

/**
 * Builds the full set of character keys to sync: the live Raider.io roster of every
 * [com.nikichxp.mtracker.domain.TrackedGuild], plus every character referenced by a
 * [com.nikichxp.mtracker.domain.TrackedPlayer] (main + alts).
 */
@Component
class RosterResolver(
    private val raiderIoClient: RaiderIoClient,
    private val trackedGuildRepository: TrackedGuildRepository,
    private val trackedPlayerRepository: TrackedPlayerRepository,
) {

    fun resolve(): ResolvedRoster {
        val guildMembers = trackedGuildRepository.findAll()
            .flatMap { guild ->
                raiderIoClient.fetchGuildRoster(guild.name, guild.realm).mapNotNull { member ->
                    val name = member.character?.name ?: return@mapNotNull null
                    val realm = member.character.realm ?: guild.realm
                    characterKey(name, realm)
                }
            }
            .toSet()

        val trackedPlayers = trackedPlayerRepository.findAll()

        val friends = trackedPlayers.filter { it.isFriend }
            .flatMap { it.characterKeys }
            .map(::normalizeKey)
            .filter { it.isNotBlank() }
            .toSet()

        val altKeys = trackedPlayers
            .flatMap { it.characterKeys }
            .map(::normalizeKey)
            .filter { it.isNotBlank() }
            .toSet()

        return ResolvedRoster(guildMembers, friends - guildMembers, altKeys - guildMembers - friends)
    }

    private fun characterKey(name: String, realm: String) = "$name-$realm"

    private fun normalizeKey(raw: String) = raw.trim()
}
