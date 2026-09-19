package com.nikichxp.mtracker.wowhead

import com.nikichxp.mtracker.domain.topgear.StatType

data class WowheadItemDto(
    val itemId: Int,
    val name: String?,
    val quality: Int?,
    val icon: String?,
    val inventorySlot: String?,
    val sourceIds: List<Int>,
    val droppedBy: String?,
    val zoneId: Int?,
)

data class WowheadTooltipDto(
    val itemId: Int,
    val name: String?,
    val itemLevel: Int?,
    val stats: Map<StatType, Int>,
    val hybridPrimary: HybridPrimaryStat?,
    val gemStats: Map<StatType, Int>,
)

data class HybridPrimaryStat(
    val options: Set<StatType>,
    val value: Int,
)
