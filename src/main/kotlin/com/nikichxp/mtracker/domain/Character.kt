package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

enum class CharacterSource { GUILD, FRIEND, ALT }

/** A single WoW character tracked via Raider.io, keyed by "Name-Realm". */
@Document(collection = "characters")
data class Character(
    @Id val id: String? = null,
    @Indexed(unique = true) val characterKey: String,
    val name: String,
    val realm: String,
    val region: String,
    val characterClass: String? = null,
    val race: String? = null,
    val faction: String? = null,
    val activeSpecName: String? = null,
    val activeSpecRole: String? = null,
    val guildName: String? = null,
    val itemLevelEquipped: Double? = null,
    val mythicPlusScore: Double? = null,
    val weeklyRuns: List<DungeonRun> = emptyList(),
    val recentRuns: List<DungeonRun> = emptyList(),
    val bestRuns: List<DungeonRun> = emptyList(),
    val source: CharacterSource,
    val lastSyncedAt: Instant? = null,
)
