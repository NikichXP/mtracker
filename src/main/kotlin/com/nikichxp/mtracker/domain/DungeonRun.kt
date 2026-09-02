package com.nikichxp.mtracker.domain

import java.time.Instant

/** A single Mythic+ keystone run, embedded on [Character] (weekly/recent/best variants). */
data class DungeonRun(
    val dungeonName: String,
    val mythicLevel: Int,
    val score: Double,
    val timed: Boolean,
    val numKeystoneUpgrades: Int,
    val completedAt: Instant?,
    val url: String? = null,
)
