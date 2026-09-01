package com.nikichxp.mtracker.raiderio.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/** Response of `GET /characters/profile`. Many upstream fields are intentionally left unmapped. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterProfileDto(
    val name: String? = null,
    val realm: String? = null,
    val race: String? = null,
    @JsonProperty("class") val characterClass: String? = null,
    @JsonProperty("active_spec_name") val activeSpecName: String? = null,
    @JsonProperty("active_spec_role") val activeSpecRole: String? = null,
    val faction: String? = null,
    val guild: GuildRefDto? = null,
    val gear: GearDto? = null,
    @JsonProperty("mythic_plus_scores_by_season") val mythicPlusScoresBySeason: List<SeasonScoreDto> = emptyList(),
    @JsonProperty("mythic_plus_ranks") val mythicPlusRanks: Map<String, Any?>? = null,
    @JsonProperty("mythic_plus_recent_runs") val mythicPlusRecentRuns: List<KeystoneRunDto> = emptyList(),
    @JsonProperty("mythic_plus_best_runs") val mythicPlusBestRuns: List<KeystoneRunDto> = emptyList(),
    @JsonProperty("mythic_plus_weekly_highest_level_runs")
    val mythicPlusWeeklyHighestLevelRuns: List<KeystoneRunDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GuildRefDto(
    val name: String? = null,
    val realm: String? = null,
    val rank: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearDto(
    @JsonProperty("item_level_equipped") val itemLevelEquipped: Double? = null,
    @JsonProperty("item_level_total") val itemLevelTotal: Double? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeasonScoreDto(
    val season: String? = null,
    val scores: SeasonScoreValuesDto? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeasonScoreValuesDto(
    val all: Double? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeystoneRunDto(
    // Raider.io returns the dungeon name as a plain string here, not a nested object.
    val dungeon: String? = null,
    @JsonProperty("mythic_level") val mythicLevel: Int? = null,
    val score: Double? = null,
    @JsonProperty("num_keystone_upgrades") val numKeystoneUpgrades: Int? = null,
    @JsonProperty("cleared_in_time") val clearedInTime: Boolean? = null,
    @JsonProperty("completed_at") val completedAt: Instant? = null,
    val url: String? = null,
)

/** Response of `GET /guilds/profile?...&fields=members`. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GuildProfileDto(
    val name: String? = null,
    val realm: String? = null,
    val members: List<GuildMemberDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GuildMemberDto(
    val character: GuildMemberCharacterDto? = null,
    val rank: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GuildMemberCharacterDto(
    val name: String? = null,
    val realm: String? = null,
    @JsonProperty("class") val characterClass: String? = null,
    val race: String? = null,
)
