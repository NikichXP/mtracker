package com.nikichxp.mtracker.web.dto

import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.domain.topgear.StatType

data class SpecOverviewDto(
    val specId: Int,
    val specName: String,
    val specSlug: String,
    val className: String,
    val role: String,
    val parseCount: Int,
    val characterCount: Int,
    val avgItemLevel: Double?,
)

data class GearItemUsageDto(
    val itemId: Int,
    val itemName: String?,
    val icon: String?,
    val source: GearSource,
    val sourceDetail: String?,
    val usageCount: Int,
    val usageShare: Double,
    val avgItemLevel: Double?,
    val stats: Map<StatType, Int>,
    val topGems: List<String>,
    val topEnchant: String?,
)

data class SlotReportDto(
    val slot: String,
    val topItems: List<GearItemUsageDto>,
)

data class SpecGearReportDto(
    val specId: Int,
    val specName: String,
    val specSlug: String,
    val className: String,
    val role: String,
    val parseCount: Int,
    val characterCount: Int,
    val slots: List<SlotReportDto>,
)

data class SlotPickDto(
    val slot: String,
    val item: GearItemUsageDto,
)

data class TalentUsageDto(
    val importString: String,
    val usageCount: Int,
)

data class PartySpecUsageDto(
    val specId: Int,
    val specName: String,
    val className: String,
    val role: String,
    val count: Int,
)

data class SpecSummaryDto(
    val specId: Int,
    val specName: String,
    val className: String,
    val role: String,
    val parseCount: Int,
    val characterCount: Int,
    val slots: List<SlotPickDto>,
    val sourceBreakdown: Map<GearSource, Int>,
    val avgStats: Map<StatType, Int>,
    val topTalentImportStrings: List<TalentUsageDto>,
    val topPartySpecs: List<PartySpecUsageDto>,
)
