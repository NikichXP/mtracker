package com.nikichxp.mtracker.domain.topgear

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.MapKeyEnumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

@Entity
@Table(
    name = "gear_item_stats",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_gear_item_stats_item_bonus", columnNames = ["item_id", "bonus_key"])
    ]
)
class GearItemStats(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "item_id", nullable = false)
    var itemId: Int = 0,
    @Column(name = "bonus_key", nullable = false)
    var bonusKey: String = "",
    var itemLevel: Int? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_item_stat_values", joinColumns = [JoinColumn(name = "stats_id")])
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "stat")
    @Column(name = "stat_value")
    var stats: Map<StatType, Int> = emptyMap(),
    var fetchedAt: Instant = Instant.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GearItemStats) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "GearItemStats(id=$id, itemId=$itemId, bonusKey='$bonusKey')"
}

interface GearItemStatsRepository : JpaRepository<GearItemStats, Long> {
    fun findByItemIdAndBonusKey(itemId: Int, bonusKey: String): GearItemStats?
}
