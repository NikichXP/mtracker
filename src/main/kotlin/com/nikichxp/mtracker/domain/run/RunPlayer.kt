package com.nikichxp.mtracker.domain.run

import com.nikichxp.mtracker.domain.player.Player
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
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

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
class RunPlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    var run: Run,
    @Column(name = "character_key", nullable = false)
    var characterKey: String = "",
    var name: String = "",
    var realm: String = "",
    var region: String? = null,
    var characterClass: String? = null,
    var spec: String? = null,
    var role: String? = null,
    var guildName: String? = null,
    var itemLevel: Double? = null,
    var rioScore: Double? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    var player: Player? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RunPlayer) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "RunPlayer(id=$id, characterKey='$characterKey')"
}

interface RunPlayerRepository : JpaRepository<RunPlayer, Long> {
    fun findByCharacterKey(characterKey: String): List<RunPlayer>
    fun findByRunIdIn(runIds: Collection<Long>): List<RunPlayer>

    fun findByPlayerIdIn(playerIds: Collection<Long>): List<RunPlayer>

    @Query(
        "select rp from RunPlayer rp where rp.characterKey in :characterKeys order by rp.run.completedAt desc"
    )
    fun findRecentByCharacterKeyIn(@Param("characterKeys") characterKeys: Collection<String>, pageable: Pageable): List<RunPlayer>
}
