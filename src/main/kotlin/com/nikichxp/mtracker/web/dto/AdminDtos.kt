package com.nikichxp.mtracker.web.dto

import java.time.Instant

data class TrackedGuildDto(
    val id: String,
    val name: String,
    val realm: String,
    val addedAt: Instant,
)

data class GuildRequest(
    val name: String,
    val realm: String,
)

data class TrackedPlayerDto(
    val id: String,
    val displayName: String,
    val characterKeys: List<String>,
    val isFriend: Boolean,
    val addedAt: Instant,
)

data class TrackedPlayerRequest(
    val displayName: String,
    val characterKeys: List<String>,
    val isFriend: Boolean = true,
)
