package com.nikichxp.mtracker.raiderio.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class RankingsResponseDto(
    val rankings: RankingsBodyDto? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RankingsBodyDto(
    val rankedCharacters: List<RankedCharacterDto> = emptyList(),
    val ui: RankingsUiDto? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RankingsUiDto(
    val page: Int? = null,
    val pageSize: Int? = null,
    val lastPage: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RankedCharacterDto(
    val rank: Int? = null,
    val score: Double? = null,
    val character: RankedCharacterInfoDto? = null,
    val guild: RosterGuildDto? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RankedCharacterInfoDto(
    val name: String? = null,
    @JsonProperty("class") val characterClass: NamedSlugDto? = null,
    val spec: RosterSpecDto? = null,
    val realm: RosterRealmDto? = null,
    val talentLoadoutText: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MythicPlusStaticDataDto(
    val seasons: List<StaticSeasonDto> = emptyList(),
    val dungeons: List<NamedSlugDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StaticSeasonDto(
    val slug: String? = null,
    val name: String? = null,
    @JsonProperty("short_name") val shortName: String? = null,
    @JsonProperty("is_main_season") val isMainSeason: Boolean = false,
    val starts: Map<String, Instant?> = emptyMap(),
    val ends: Map<String, Instant?> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RaidingStaticDataDto(
    val raids: List<StaticRaidDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StaticRaidDto(
    val slug: String? = null,
    val name: String? = null,
    val encounters: List<NamedSlugDto> = emptyList(),
)
