package com.nikichxp.mtracker.domain.weekly

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
class WeeklySnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var weekKey: String = "",
    var characterKey: String = "",
    var mythicPlusScore: Double? = null,
    var weeklyRunsCount: Int = 0,
    var weeklyHighestLevel: Int = 0,
    var capturedAt: Instant = Instant.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WeeklySnapshot) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "WeeklySnapshot(id=$id, weekKey='$weekKey', characterKey='$characterKey')"
}
