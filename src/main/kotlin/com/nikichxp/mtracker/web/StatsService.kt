package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.domain.Character
import com.nikichxp.mtracker.domain.CharacterRepository
import com.nikichxp.mtracker.domain.Player
import com.nikichxp.mtracker.domain.PlayerRepository
import com.nikichxp.mtracker.domain.WeekKeyCalculator
import com.nikichxp.mtracker.domain.WeeklySnapshotRepository
import com.nikichxp.mtracker.web.dto.CharacterDto
import com.nikichxp.mtracker.web.dto.DungeonRunDto
import com.nikichxp.mtracker.web.dto.PlayerDetailDto
import com.nikichxp.mtracker.web.dto.PlayerOverviewDto
import com.nikichxp.mtracker.web.dto.WeeklyPlayerStatsDto
import org.springframework.stereotype.Service

/** Builds the read-only DTOs served by [StatsController] out of the stored domain data. */
@Service
class StatsService(
    private val playerRepository: PlayerRepository,
    private val characterRepository: CharacterRepository,
    private val weeklySnapshotRepository: WeeklySnapshotRepository,
) {

    fun overview(): List<PlayerOverviewDto> {
        val players = playerRepository.findAll()
        val allCharacterKeys = players.flatMap { it.characterKeys }.toSet()
        val charactersByKey = characterRepository.findByCharacterKeyIn(allCharacterKeys).associateBy { it.characterKey }
        return players.map { toOverview(it, charactersByKey) }
    }

    fun playerDetail(playerKey: String): PlayerDetailDto? {
        val player = playerRepository.findByPlayerKey(playerKey) ?: return null
        val characters = characterRepository.findByCharacterKeyIn(player.characterKeys)
        return PlayerDetailDto(
            playerKey = player.playerKey,
            displayName = player.displayName,
            characters = characters.map(::toCharacterDto),
        )
    }

    fun weeklyStats(weekKey: String?): List<WeeklyPlayerStatsDto> {
        val effectiveWeekKey = weekKey ?: WeekKeyCalculator.currentWeekKey()
        val snapshots = weeklySnapshotRepository.findByWeekKey(effectiveWeekKey)
        if (snapshots.isEmpty()) return emptyList()

        val players = playerRepository.findAll()
        val playerByCharacterKey = mutableMapOf<String, Player>()
        for (player in players) {
            for (characterKey in player.characterKeys) playerByCharacterKey[characterKey] = player
        }

        val snapshotsByPlayer = snapshots.groupBy { playerByCharacterKey[it.characterKey]?.playerKey ?: it.characterKey }
        return snapshotsByPlayer.map { (playerKey, playerSnapshots) ->
            val displayName = playerByCharacterKey[playerSnapshots.first().characterKey]?.displayName ?: playerKey
            WeeklyPlayerStatsDto(
                weekKey = effectiveWeekKey,
                playerKey = playerKey,
                displayName = displayName,
                weeklyRunsCount = playerSnapshots.sumOf { it.weeklyRunsCount },
                weeklyHighestLevel = playerSnapshots.maxOfOrNull { it.weeklyHighestLevel } ?: 0,
                totalScore = playerSnapshots.mapNotNull { it.mythicPlusScore }.maxOrNull() ?: 0.0,
            )
        }
    }

    fun availableWeeks(): List<String> =
        weeklySnapshotRepository.findAll().map { it.weekKey }.distinct().sortedDescending()

    private fun toOverview(player: Player, charactersByKey: Map<String, Character>): PlayerOverviewDto {
        val characters = player.characterKeys.mapNotNull { charactersByKey[it] }
        val bestCharacter = characters.maxByOrNull { it.mythicPlusScore ?: 0.0 }
        return PlayerOverviewDto(
            playerKey = player.playerKey,
            displayName = player.displayName,
            isFriend = player.isFriend,
            isGuildMember = player.isGuildMember,
            totalScore = characters.mapNotNull { it.mythicPlusScore }.maxOrNull() ?: 0.0,
            weeklyRunsCount = characters.sumOf { it.weeklyRuns.size },
            weeklyHighestLevel = characters.flatMap { it.weeklyRuns }.maxOfOrNull { it.mythicLevel } ?: 0,
            maxItemLevel = characters.mapNotNull { it.itemLevelEquipped }.maxOrNull() ?: 0.0,
            activeSpecName = bestCharacter?.activeSpecName,
            activeSpecRole = bestCharacter?.activeSpecRole,
            characterCount = characters.size,
            lastSyncedAt = characters.mapNotNull { it.lastSyncedAt }.maxOrNull(),
        )
    }

    private fun toCharacterDto(character: Character) = CharacterDto(
        characterKey = character.characterKey,
        name = character.name,
        realm = character.realm,
        characterClass = character.characterClass,
        activeSpecName = character.activeSpecName,
        activeSpecRole = character.activeSpecRole,
        itemLevelEquipped = character.itemLevelEquipped,
        mythicPlusScore = character.mythicPlusScore,
        weeklyRuns = character.weeklyRuns.map {
            DungeonRunDto(
                dungeonName = it.dungeonName,
                mythicLevel = it.mythicLevel,
                score = it.score,
                timed = it.timed,
                completedAt = it.completedAt,
            )
        },
        lastSyncedAt = character.lastSyncedAt,
    )
}
