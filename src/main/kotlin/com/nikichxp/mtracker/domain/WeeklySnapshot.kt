package com.nikichxp.mtracker.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "weekly_snapshots",
    uniqueConstraints = [UniqueConstraint(name = "uk_weekly_snapshot", columnNames = ["weekKey", "characterKey"])]
)
data class WeeklySnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val weekKey: String = "",
    val characterKey: String = "",
    val mythicPlusScore: Double? = null,
    val weeklyRunsCount: Int = 0,
    val weeklyHighestLevel: Int = 0,
    val capturedAt: Instant = Instant.now(),
)
