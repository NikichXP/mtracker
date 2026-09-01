package com.nikichxp.mtracker.web.dto

import java.time.Instant

data class PlayerOverviewDto(
    val playerKey: String,
    val displayName: String,
    val isFriend: Boolean,
    val isGuildMember: Boolean,
    val totalScore: Double,
    val weeklyRunsCount: Int,
    val weeklyHighestLevel: Int,
    val maxItemLevel: Double,
    val activeSpecName: String?,
    val activeSpecRole: String?,
    val characterCount: Int,
    val lastSyncedAt: Instant?,
)

data class CharacterDto(
    val characterKey: String,
    val name: String,
    val realm: String,
    val characterClass: String?,
    val activeSpecName: String?,
    val activeSpecRole: String?,
    val itemLevelEquipped: Double?,
    val mythicPlusScore: Double?,
    val weeklyRuns: List<DungeonRunDto>,
    val lastSyncedAt: Instant?,
)

data class DungeonRunDto(
    val dungeonName: String,
    val mythicLevel: Int,
    val score: Double,
    val timed: Boolean,
    val completedAt: Instant?,
)

data class PlayerDetailDto(
    val playerKey: String,
    val displayName: String,
    val characters: List<CharacterDto>,
)

data class WeeklyPlayerStatsDto(
    val weekKey: String,
    val playerKey: String,
    val displayName: String,
    val weeklyRunsCount: Int,
    val weeklyHighestLevel: Int,
    val totalScore: Double,
)
