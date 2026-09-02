package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "tracked_players")
data class TrackedPlayer(
    @Id val id: String? = null,
    val displayName: String,
    val characterKeys: List<String>,
    val isFriend: Boolean = true,
    val addedAt: Instant = Instant.now(),
)
