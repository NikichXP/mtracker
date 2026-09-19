package com.nikichxp.mtracker.domain.topgear

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Embeddable
data class GearSocket(
    val gemId: Int = 0,
    val name: String? = null,
)

@Embeddable
data class GearEnchantment(
    val enchantId: Int = 0,
    val name: String? = null,
)

@Entity
@Table(name = "gear_snapshot_items")
class GearSnapshotItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    var snapshot: GearSnapshot? = null,
    var slot: String = "",
    var itemId: Int = 0,
    var itemLevel: Int = 0,
    var itemName: String? = null,
    var itemQuality: Int? = null,
    var tierSetId: String? = null,
    @Enumerated(EnumType.STRING)
    var source: GearSource = GearSource.UNKNOWN,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_snapshot_item_bonuses", joinColumns = [JoinColumn(name = "snapshot_item_id")])
    @Column(name = "bonus_id")
    var bonusIds: List<Int> = emptyList(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_snapshot_item_gems", joinColumns = [JoinColumn(name = "snapshot_item_id")])
    var gems: List<GearSocket> = emptyList(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_snapshot_item_enchants", joinColumns = [JoinColumn(name = "snapshot_item_id")])
    var enchants: List<GearEnchantment> = emptyList(),
) {
    // The snapshot back-reference would recurse through data-class equals/hashCode/toString.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GearSnapshotItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "GearSnapshotItem(id=$id, slot=$slot, itemId=$itemId)"
}
