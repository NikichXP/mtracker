package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.domain.topgear.GearItemCatalog
import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.raiderio.dto.GearItemDto
import com.nikichxp.mtracker.wowhead.WowheadItemDto
import org.springframework.stereotype.Service

@Service
class GearSourceResolver(
    private val raidEncounterCatalog: RaidEncounterCatalog,
) {

    suspend fun classify(wowheadItem: WowheadItemDto): GearSource {
        if (1 in wowheadItem.sourceIds) {
            return GearSource.CRAFTED
        }
        if (raidEncounterCatalog.isRaidEncounter(wowheadItem.droppedBy)) {
            return GearSource.RAID
        }
        if (2 in wowheadItem.sourceIds) {
            return GearSource.KEYS
        }
        if (5 in wowheadItem.sourceIds) {
            return GearSource.VENDOR
        }
        if (4 in wowheadItem.sourceIds) {
            return GearSource.QUEST
        }
        if (3 in wowheadItem.sourceIds) {
            return GearSource.PVP
        }
        return GearSource.UNKNOWN
    }

    fun sourceDetail(wowheadItem: WowheadItemDto?, source: GearSource): String? {
        wowheadItem?.droppedBy?.let { return it }
        if (source == GearSource.CRAFTED) {
            return "crafted"
        }
        return wowheadItem?.zoneId?.let { "zone:$it" }
    }

    fun effectiveSource(catalog: GearItemCatalog, gearItem: GearItemDto): GearSource {
        if (gearItem.tier != null) {
            return GearSource.SET
        }
        return catalog.source
    }
}
