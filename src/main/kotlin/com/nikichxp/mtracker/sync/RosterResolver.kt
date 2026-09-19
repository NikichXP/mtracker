package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.domain.tracking.TrackedGuildRepository
import com.nikichxp.mtracker.domain.tracking.TrackedPlayerRepository
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import org.springframework.stereotype.Component

data class ResolvedRoster(
    val guildMemberKeys: Set<String>,
    val friendKeys: Set<String>,
    val altKeys: Set<String>,
) {
    val allKeys: Set<String> get() = guildMemberKeys + friendKeys + altKeys
}

@Component
class RosterResolver(
    private val raiderIoService: IRaiderIoService,
    private val trackedGuildRepository: TrackedGuildRepository,
    private val trackedPlayerRepository: TrackedPlayerRepository,
) {

    suspend fun resolve(): ResolvedRoster {
        val guildMembers = buildSet {
            for (guild in trackedGuildRepository.findAll()) {
                for (member in raiderIoService.fetchGuildRoster(guild.name, guild.realm)) {
                    val name = member.character?.name ?: continue
                    val realm = member.character.realm ?: guild.realm
                    add(characterKey(name, realm))
                }
            }
        }

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
