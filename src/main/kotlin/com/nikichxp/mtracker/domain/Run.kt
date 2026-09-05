package com.nikichxp.mtracker.domain

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
data class Run(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val season: String = "",
    @Column(name = "keystone_run_id", nullable = false)
    val keystoneRunId: Long = 0,
    val dungeonName: String = "",
    val dungeonShortName: String? = null,
    val mythicLevel: Int = 0,
    val score: Double? = null,
    val timed: Boolean = false,
    val numKeystoneUpgrades: Int = 0,
    val clearTimeMs: Long? = null,
    val keystoneTimeMs: Long? = null,
    val completedAt: Instant? = null,
    val url: String? = null,
)

interface RunRepository : JpaRepository<Run, Long> {
    fun findBySeasonAndKeystoneRunId(season: String, keystoneRunId: Long): Run?
    fun existsBySeasonAndKeystoneRunId(season: String, keystoneRunId: Long): Boolean
}
