package com.nikichxp.mtracker.domain.run

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

/**
 * A single Mythic+ keystone run, one row per run (shared by all roster members).
 * Identity is the Raider.io (season, keystoneRunId) pair, parsed from the run URL.
 */
@Entity
@Table(
    name = "runs",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_runs_season_keystone_run", columnNames = ["season", "keystone_run_id"])
    ],
    indexes = [
        Index(name = "idx_runs_completed_at", columnList = "completedAt")
    ]
)
class Run(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var season: String = "",
    @Column(name = "keystone_run_id", nullable = false)
    var keystoneRunId: Long = 0,
    var dungeonName: String = "",
    var dungeonShortName: String? = null,
    var mythicLevel: Int = 0,
    var score: Double? = null,
    var timed: Boolean = false,
    var numKeystoneUpgrades: Int = 0,
    var clearTimeMs: Long? = null,
    var keystoneTimeMs: Long? = null,
    var completedAt: Instant? = null,
    var url: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Run) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "Run(id=$id, season='$season', keystoneRunId=$keystoneRunId)"
}

interface RunRepository : JpaRepository<Run, Long> {
    fun findBySeasonAndKeystoneRunId(season: String, keystoneRunId: Long): Run?
    fun existsBySeasonAndKeystoneRunId(season: String, keystoneRunId: Long): Boolean
}
