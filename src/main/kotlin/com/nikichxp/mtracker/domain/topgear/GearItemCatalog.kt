package com.nikichxp.mtracker.domain.topgear

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

@Entity
@Table(name = "gear_item_catalog")
class GearItemCatalog(
    @Id
    var itemId: Int = 0,
    var englishName: String? = null,
    var quality: Int? = null,
    var icon: String? = null,
    var inventorySlot: String? = null,
    @Enumerated(EnumType.STRING)
    var source: GearSource = GearSource.UNKNOWN,
    var sourceDetail: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_item_catalog_sources", joinColumns = [JoinColumn(name = "item_id")])
    @Column(name = "source_id")
    var wowheadSourceIds: List<Int> = emptyList(),
    var fetchedAt: Instant = Instant.now(),
    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GearItemCatalog) return false
        return itemId == other.itemId
    }

    override fun hashCode(): Int = itemId.hashCode()

    override fun toString(): String = "GearItemCatalog(itemId=$itemId, englishName=$englishName)"
}

interface GearItemCatalogRepository : JpaRepository<GearItemCatalog, Int>
