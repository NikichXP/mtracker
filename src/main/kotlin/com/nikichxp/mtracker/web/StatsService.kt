package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.domain.Character
import com.nikichxp.mtracker.domain.CharacterRepository
import com.nikichxp.mtracker.domain.Player
import com.nikichxp.mtracker.domain.PlayerRepository
import com.nikichxp.mtracker.domain.Run
import com.nikichxp.mtracker.domain.RunPlayer
import com.nikichxp.mtracker.domain.RunPlayerRepository
import com.nikichxp.mtracker.domain.WeekKeyCalculator
import com.nikichxp.mtracker.domain.WeeklySnapshotRepository
import com.nikichxp.mtracker.web.dto.CharacterDto
import com.nikichxp.mtracker.web.dto.DungeonRunDto
import com.nikichxp.mtracker.web.dto.PlayerDetailDto
import com.nikichxp.mtracker.web.dto.PlayerOverviewDto
import com.nikichxp.mtracker.web.dto.RecentRunDto
import com.nikichxp.mtracker.web.dto.RunRosterMemberDto
import com.nikichxp.mtracker.web.dto.WeeklyPlayerStatsDto
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Builds the read-only DTOs served by [com.nikichxp.mtracker.api.StatsController] out of the stored domain data. */
@Service
@Transactional(readOnly = true)
class StatsService(
    private val playerRepository: PlayerRepository,
    private val characterRepository: CharacterRepository,
    private val weeklySnapshotRepository: WeeklySnapshotRepository,
    private val runPlayerRepository: RunPlayerRepository,
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

    /** Most recent stored Mythic+ runs played by any of [playerKey]'s characters, newest first, with full roster. */
    fun recentRuns(playerKey: String, limit: Int = 10): List<RecentRunDto>? {
        val player = playerRepository.findByPlayerKey(playerKey) ?: return null
        if (player.characterKeys.isEmpty()) return emptyList()

        val ownRuns = runPlayerRepository.findRecentByCharacterKeyIn(player.characterKeys, PageRequest.of(0, limit))
        if (ownRuns.isEmpty()) return emptyList()

        val runIds = ownRuns.mapNotNull { it.run.id }.distinct()
        val rosterByRunId = runPlayerRepository.findByRunIdIn(runIds).groupBy { it.run.id }

        return ownRuns
            .distinctBy { it.run.id }
            .sortedByDescending { it.run.completedAt }
            .map { toRecentRunDto(it.run, rosterByRunId[it.run.id].orEmpty()) }
    }

    private fun toRecentRunDto(run: Run, roster: List<RunPlayer>) = RecentRunDto(
        season = run.season,
        keystoneRunId = run.keystoneRunId,
        dungeonName = run.dungeonName,
        dungeonShortName = run.dungeonShortName,
        mythicLevel = run.mythicLevel,
        score = run.score,
        timed = run.timed,
        numKeystoneUpgrades = run.numKeystoneUpgrades,
        completedAt = run.completedAt,
        url = run.url,
        roster = roster.map { member ->
            RunRosterMemberDto(
                characterKey = member.characterKey,
                name = member.name,
                realm = member.realm,
                characterClass = member.characterClass,
                spec = member.spec,
                role = member.role,
                guildName = member.guildName,
                itemLevel = member.itemLevel,
                rioScore = member.rioScore,
                isTrackedPlayer = member.player != null,
                playerKey = member.player?.playerKey,
            )
        },
    )

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
        weeklySnapshotRepository.findDistinctWeekKeys()

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
        weeklyRuns = character.weeklyRuns.map { run ->
            DungeonRunDto(
                dungeonName = run.dungeonName,
                mythicLevel = run.mythicLevel,
                score = run.score,
                timed = run.timed,
                completedAt = run.completedAt,
            )
        },
        lastSyncedAt = character.lastSyncedAt,
    )
}
