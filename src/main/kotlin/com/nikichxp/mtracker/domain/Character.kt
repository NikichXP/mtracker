package com.nikichxp.mtracker.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.Instant

enum class CharacterSource { GUILD, FRIEND, ALT }

/** A single WoW character tracked via Raider.io, keyed by "Name-Realm". */
@Entity
@Table(
    name = "characters",
    indexes = [
        Index(name = "idx_characters_character_key", columnList = "characterKey", unique = true)
    ]
)
data class Character(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val characterKey: String = "",
    val name: String = "",
    val realm: String = "",
    val region: String = "",
    val characterClass: String? = null,
    val race: String? = null,
    val faction: String? = null,
    val activeSpecName: String? = null,
    val activeSpecRole: String? = null,
    val guildName: String? = null,
    val itemLevelEquipped: Double? = null,
    val mythicPlusScore: Double? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "character_weekly_runs", joinColumns = [JoinColumn(name = "character_id")])
    @Fetch(FetchMode.SUBSELECT)
    val weeklyRuns: List<DungeonRun> = emptyList(),
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "character_recent_runs", joinColumns = [JoinColumn(name = "character_id")])
    @Fetch(FetchMode.SUBSELECT)
    val recentRuns: List<DungeonRun> = emptyList(),
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "character_best_runs", joinColumns = [JoinColumn(name = "character_id")])
    @Fetch(FetchMode.SUBSELECT)
    val bestRuns: List<DungeonRun> = emptyList(),
    @Enumerated(EnumType.STRING)
    val source: CharacterSource = CharacterSource.GUILD,
    val lastSyncedAt: Instant? = null,
)
