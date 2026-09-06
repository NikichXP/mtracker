package com.nikichxp.mtracker.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class PlayerOverviewDto(
    val playerKey: String,
    val displayName: String,
    @get:JsonProperty("isFriend") @param:JsonProperty("isFriend") val isFriend: Boolean,
    @get:JsonProperty("isGuildMember") @param:JsonProperty("isGuildMember") val isGuildMember: Boolean,
    val totalScore: Double,
    val weeklyRunsCount: Int,
    val weeklyHighestLevel: Int,
    val maxItemLevel: Double,
    val activeSpecName: String?,
    val activeSpecRole: String?,
    val characterCount: Int,
    val lastSyncedAt: Instant?,
    /**
     * Average, across this player's stored runs, of the share of the roster made up of *other*
     * tracked players (0 = always pugs, 1 = always full pre-made with the tracked roster).
     * `null` when no runs are stored for this player yet.
     */
    val buddyScore: Double?,
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
    @JsonProperty("timed") val timed: Boolean,
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

/** One of the 5 roster members of a [RecentRunDto], as snapshotted at the time the run was fetched. */
data class RunRosterMemberDto(
    val characterKey: String,
    val name: String,
    val realm: String,
    val characterClass: String?,
    val spec: String?,
    val role: String?,
    val guildName: String?,
    val itemLevel: Double?,
    val rioScore: Double?,
    @get:JsonProperty("isTrackedPlayer") @param:JsonProperty("isTrackedPlayer") val isTrackedPlayer: Boolean,
    val playerKey: String?,
)

/** A single Mythic+ keystone run played by (one of the characters of) a player, with its full roster. */
data class RecentRunDto(
    val season: String,
    val keystoneRunId: Long,
    val dungeonName: String,
    val dungeonShortName: String?,
    val mythicLevel: Int,
    val score: Double?,
    @JsonProperty("timed") val timed: Boolean,
    val numKeystoneUpgrades: Int,
    val completedAt: Instant?,
    val url: String?,
    val roster: List<RunRosterMemberDto>,
)
