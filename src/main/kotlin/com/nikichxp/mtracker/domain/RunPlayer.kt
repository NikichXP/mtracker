package com.nikichxp.mtracker.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository

/**
 * One roster member of a [Run] (5 rows per run). Stores a snapshot of the character's state
 * at the moment of fetch (rio score, gear, spec, guild). [player] is an FK to the tracked
 * player if this roster member is one of the tracked characters, `null` otherwise.
 */
@Entity
@Table(
    name = "run_players",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_run_players_run_character", columnNames = ["run_id", "character_key"])
    ],
    indexes = [
        Index(name = "idx_run_players_character_key", columnList = "character_key"),
        Index(name = "idx_run_players_player_id", columnList = "player_id")
    ]
)
data class RunPlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    val run: Run,
    @Column(name = "character_key", nullable = false)
    val characterKey: String = "",
    val name: String = "",
    val realm: String = "",
    val region: String? = null,
    val characterClass: String? = null,
    val spec: String? = null,
    val role: String? = null,
    val guildName: String? = null,
    val itemLevel: Double? = null,
    /** Mythic+ score of this character at the point of fetch (from run roster ranks). */
    val rioScore: Double? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    val player: Player? = null,
)

interface RunPlayerRepository : JpaRepository<RunPlayer, Long> {
    fun findByCharacterKey(characterKey: String): List<RunPlayer>
    fun findByRunIdIn(runIds: Collection<Long>): List<RunPlayer>
}
