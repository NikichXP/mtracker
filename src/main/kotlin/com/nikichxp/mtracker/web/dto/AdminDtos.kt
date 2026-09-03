package com.nikichxp.mtracker.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TrackedGuildDto(
    val id: Long,
    val name: String,
    val realm: String,
    val addedAt: Instant,
)

data class GuildRequest(
    val name: String,
    val realm: String,
)

data class TrackedPlayerDto(
    val id: Long,
    val displayName: String,
    val characterKeys: List<String>,
    @get:JsonProperty("isFriend") @param:JsonProperty("isFriend") val isFriend: Boolean,
    val addedAt: Instant,
)

data class TrackedPlayerRequest(
    val displayName: String,
    val characterKeys: List<String>,
    @get:JsonProperty("isFriend") @param:JsonProperty("isFriend") val isFriend: Boolean = true,
)
