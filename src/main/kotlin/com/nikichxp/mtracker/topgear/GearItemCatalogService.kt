package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.domain.topgear.GearItemCatalog
import com.nikichxp.mtracker.domain.topgear.GearItemCatalogRepository
import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.raiderio.dto.GearItemDto
import com.nikichxp.mtracker.wowhead.IWowheadService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GearItemCatalogService(
    private val repository: GearItemCatalogRepository,
    private val wowheadService: IWowheadService,
    private val gearSourceResolver: GearSourceResolver,
) {

    suspend fun resolve(gearItem: GearItemDto): GearItemCatalog {
        repository.findById(gearItem.itemId).orElse(null)?.let { return it }
        val wowheadItem = wowheadService.fetchItem(gearItem.itemId)
            ?: return transientFallback(gearItem)
        val source = gearSourceResolver.classify(wowheadItem)
        val catalog = GearItemCatalog(
            itemId = gearItem.itemId,
            englishName = wowheadItem.name ?: gearItem.name,
            quality = wowheadItem.quality ?: gearItem.itemQuality,
            icon = wowheadItem.icon ?: gearItem.icon,
            inventorySlot = wowheadItem.inventorySlot,
            source = source,
            sourceDetail = gearSourceResolver.sourceDetail(wowheadItem, source),
            wowheadSourceIds = wowheadItem.sourceIds,
            fetchedAt = Instant.now(),
        )
        return try {
            repository.save(catalog)
        } catch (e: DataIntegrityViolationException) {
            repository.findById(gearItem.itemId).orElse(catalog)
        }
    }

    private fun transientFallback(gearItem: GearItemDto) = GearItemCatalog(
        itemId = gearItem.itemId,
        englishName = gearItem.name,
        quality = gearItem.itemQuality,
        icon = gearItem.icon,
        source = GearSource.UNKNOWN,
        fetchedAt = Instant.now(),
    )
}
