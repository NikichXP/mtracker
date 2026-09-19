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
    @JsonProperty("talentLoadout") val talentLoadout: TalentLoadoutDto? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TalentLoadoutDto(
    @JsonProperty("loadout_spec_id") val loadoutSpecId: Int? = null,
    @JsonProperty("loadout_text") val loadoutText: String? = null,
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
    val items: Map<String, GearItemDto> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearItemDto(
    @JsonProperty("item_id") val itemId: Int = 0,
    @JsonProperty("item_level") val itemLevel: Int = 0,
    val name: String? = null,
    @JsonProperty("item_quality") val itemQuality: Int? = null,
    val icon: String? = null,
    val tier: String? = null,
    val bonuses: List<Int> = emptyList(),
    val gems: List<Int> = emptyList(),
    @JsonProperty("gems_detail") val gemsDetail: List<NamedIdDto> = emptyList(),
    val enchants: List<Int> = emptyList(),
    @JsonProperty("enchants_detail") val enchantsDetail: List<NamedIdDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NamedIdDto(
    val id: Int? = null,
    val name: String? = null,
    val icon: String? = null,
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
    val spec: RosterSpecDto? = null,
    val role: String? = null,
    @JsonProperty("short_name") val shortName: String? = null,
)

/** Response of `GET /mythic-plus/run-details?season=...&id=...`. Many fields intentionally unmapped. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RunDetailsDto(
    val season: String? = null,
    @JsonProperty("keystone_run_id") val keystoneRunId: Long? = null,
    @JsonProperty("mythic_level") val mythicLevel: Int? = null,
    @JsonProperty("clear_time_ms") val clearTimeMs: Long? = null,
    @JsonProperty("keystone_time_ms") val keystoneTimeMs: Long? = null,
    @JsonProperty("completed_at") val completedAt: Instant? = null,
    @JsonProperty("num_chests") val numChests: Int? = null,
    val score: Double? = null,
    val dungeon: RunDungeonDto? = null,
    val roster: List<RunRosterMemberDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RunDungeonDto(
    val name: String? = null,
    @JsonProperty("short_name") val shortName: String? = null,
    val slug: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RunRosterMemberDto(
    val character: RosterCharacterDto? = null,
    /** Role in the group (tank, healer, dps). */
    val role: String? = null,
    val guild: RosterGuildDto? = null,
    val items: RosterItemsDto? = null,
    val ranks: RosterRanksDto? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterCharacterDto(
    val name: String? = null,
    @JsonProperty("class") val characterClass: NamedSlugDto? = null,
    val spec: RosterSpecDto? = null,
    val realm: RosterRealmDto? = null,
    val region: RosterRegionDto? = null,
    val faction: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NamedSlugDto(
    val name: String? = null,
    val slug: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterSpecDto(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val role: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterRealmDto(
    val name: String? = null,
    val slug: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterRegionDto(
    val name: String? = null,
    @JsonProperty("short_name") val shortName: String? = null,
    val slug: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterGuildDto(
    val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterItemsDto(
    @JsonProperty("item_level_equipped") val itemLevelEquipped: Double? = null,
    val items: Map<String, GearItemDto> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterRanksDto(
    /** M+ score of the character at the point of fetch. */
    val score: Double? = null,
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
