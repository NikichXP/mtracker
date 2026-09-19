package com.nikichxp.mtracker.domain.character

import jakarta.persistence.Embeddable
import java.time.Instant

/** A single Mythic+ keystone run, embedded on [Character] (weekly/recent/best variants). */
@Embeddable
data class DungeonRun(
    val dungeonName: String = "",
    val mythicLevel: Int = 0,
    val score: Double = 0.0,
    val timed: Boolean = false,
    val numKeystoneUpgrades: Int = 0,
    val completedAt: Instant? = null,
    val url: String? = null,
)
