package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * A real person tracked outside of (or in addition to) guild roster sync - e.g. a friend, or a
 * guild member whose alts should be grouped under one [Player]. [characterKeys] is "main first,
 * then alts"; the first entry becomes the resulting [Player.playerKey].
 *
 * Replaces the old `mtracker.friends` / `mtracker.player-links` static YAML config - alt-linking
 * is still entirely manual (Raider.io exposes no such relation), but now managed at runtime via
 * [com.nikichxp.mtracker.web.TrackedPlayerAdminController] instead of being baked into config.
 *
 * Region is a single global setting (`mtracker.region` / `MTRACKER_REGION`), not per-player.
 */
@Document(collection = "tracked_players")
data class TrackedPlayer(
    @Id val id: String? = null,
    val displayName: String,
    val characterKeys: List<String>,
    val isFriend: Boolean = true,
    val addedAt: Instant = Instant.now(),
)
