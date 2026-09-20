package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.topgear.GearItemCatalog
import com.nikichxp.mtracker.domain.topgear.GearItemStats
import com.nikichxp.mtracker.domain.topgear.GearItemStatsRepository
import com.nikichxp.mtracker.domain.topgear.GearItemCatalogRepository
import com.nikichxp.mtracker.domain.topgear.GearSnapshot
import com.nikichxp.mtracker.domain.topgear.GearSnapshotItem
import com.nikichxp.mtracker.domain.topgear.GearSnapshotRepository
import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.domain.topgear.StatType
import com.nikichxp.mtracker.web.dto.GearItemUsageDto
import com.nikichxp.mtracker.web.dto.PartySpecUsageDto
import com.nikichxp.mtracker.web.dto.SlotPickDto
import com.nikichxp.mtracker.web.dto.SlotReportDto
import com.nikichxp.mtracker.web.dto.SpecGearReportDto
import com.nikichxp.mtracker.web.dto.SpecOverviewDto
import com.nikichxp.mtracker.web.dto.SpecSummaryDto
import com.nikichxp.mtracker.web.dto.TalentUsageDto
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import kotlin.math.roundToInt

@Service
class GearScopeService(
    private val snapshotRepository: GearSnapshotRepository,
    private val catalogRepository: GearItemCatalogRepository,
    private val statsRepository: GearItemStatsRepository,
    private val props: MtrackerProperties,
    private val conversionService: ConversionService,
) {

    fun specOverview(): List<SpecOverviewDto> {
        return snapshotRepository.specOverview()
            .mapNotNull { conversionService.convert(it, SpecOverviewDto::class.java) }
            .sortedByDescending { it.parseCount }
    }

    fun specItems(specId: Int, excludeRaid: Boolean, limit: Int?): SpecGearReportDto? {
        val snapshots = snapshotRepository.findBySpecId(specId)
        if (snapshots.isEmpty()) {
            return null
        }
        val catalogs = catalogsFor(snapshots)
        val stats = statsFor(snapshots)
        val slots = slotReports(snapshots, catalogs, stats, excludeRaid, limit ?: props.topGear.topItemsPerSlot)
        val first = snapshots.first()
        return SpecGearReportDto(
            specId = specId,
            specName = first.specName,
            specSlug = first.specSlug,
            className = first.className,
            role = first.role,
            parseCount = snapshots.size,
            characterCount = snapshots.map { it.characterKey }.distinct().size,
            slots = slots,
        )
    }

    fun specSummary(specId: Int, excludeRaid: Boolean): SpecSummaryDto? {
        val snapshots = snapshotRepository.findBySpecId(specId)
        if (snapshots.isEmpty()) {
            return null
        }
        val catalogs = catalogsFor(snapshots)
        val stats = statsFor(snapshots)
        val slotReports = slotReports(snapshots, catalogs, stats, excludeRaid, 1)
        val visibleItems = snapshots.flatMap { it.items }.filter { include(it, excludeRaid) }
        val first = snapshots.first()
        return SpecSummaryDto(
            specId = specId,
            specName = first.specName,
            className = first.className,
            role = first.role,
            parseCount = snapshots.size,
            characterCount = snapshots.map { it.characterKey }.distinct().size,
            slots = slotReports.mapNotNull { slot ->
                slot.topItems.firstOrNull()?.let { SlotPickDto(slot.slot, it) }
            },
            sourceBreakdown = visibleItems.groupingBy { it.source }.eachCount(),
            avgStats = averageStats(snapshots),
            topTalentImportStrings = snapshots
                .mapNotNull { it.talentImportString }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { TalentUsageDto(it.key, it.value) },
            topPartySpecs = snapshots
                .flatMap { it.partySpecs }
                .groupingBy { it.specId }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(8)
                .map { (partySpecId, count) ->
                    val member = snapshots.flatMap { it.partySpecs }.first { it.specId == partySpecId }
                    PartySpecUsageDto(partySpecId, member.specName, member.className, member.role, count)
                },
        )
    }

    private fun slotReports(
        snapshots: List<GearSnapshot>,
        catalogs: Map<Int, GearItemCatalog>,
        stats: Map<Pair<Int, String>, GearItemStats>,
        excludeRaid: Boolean,
        limit: Int,
    ): List<SlotReportDto> {
        val itemsBySlot = snapshots.flatMap { it.items }.filter { include(it, excludeRaid) }.groupBy { it.slot }
        return itemsBySlot.map { (slot, items) ->
            val slotSnapshots = snapshots.count { s -> s.items.any { it.slot == slot } }
            val topItems = items.groupBy { it.itemId }
                .map { (itemId, usages) -> usageDto(itemId, usages, slotSnapshots, catalogs, stats) }
                .sortedByDescending { it.usageCount }
                .take(limit)
            SlotReportDto(slot, topItems)
        }.sortedBy { it.slot }
    }

    private fun usageDto(
        itemId: Int,
        usages: List<GearSnapshotItem>,
        slotSnapshots: Int,
        catalogs: Map<Int, GearItemCatalog>,
        stats: Map<Pair<Int, String>, GearItemStats>,
    ): GearItemUsageDto {
        val catalog = catalogs[itemId]
        val mostCommonBonusKey = usages
            .groupingBy { it.bonusIds.sorted().joinToString(":") }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: ""
        return GearItemUsageDto(
            itemId = itemId,
            itemName = usages.firstNotNullOfOrNull { it.itemName } ?: catalog?.englishName,
            icon = catalog?.icon,
            source = usages.first().source,
            sourceDetail = catalog?.sourceDetail,
            usageCount = usages.size,
            usageShare = if (slotSnapshots > 0) usages.size.toDouble() / slotSnapshots else 0.0,
            avgItemLevel = usages.map { it.itemLevel }.average(),
            stats = stats[itemId to mostCommonBonusKey]?.stats ?: emptyMap(),
            topGems = usages.flatMap { it.gems }
                .groupingBy { it.gemId }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { entry -> usages.flatMap { it.gems }.first { it.gemId == entry.key }.name ?: entry.key.toString() },
            topEnchant = usages.flatMap { it.enchants }
                .groupingBy { it.enchantId }
                .eachCount()
                .entries
                .maxByOrNull { it.value }
                ?.let { entry -> usages.flatMap { it.enchants }.first { it.enchantId == entry.key }.name ?: entry.key.toString() },
        )
    }

    private fun catalogsFor(snapshots: List<GearSnapshot>): Map<Int, GearItemCatalog> {
        val itemIds = snapshots.flatMap { s -> s.items.map { it.itemId } }.distinct()
        return catalogRepository.findAllById(itemIds).associateBy { it.itemId }
    }

    private fun statsFor(snapshots: List<GearSnapshot>): Map<Pair<Int, String>, GearItemStats> {
        val itemIds = snapshots.flatMap { s -> s.items.map { it.itemId } }.distinct()
        return statsRepository.findByItemIdIn(itemIds).associateBy { it.itemId to it.bonusKey }
    }

    private fun include(item: GearSnapshotItem, excludeRaid: Boolean): Boolean {
        return !excludeRaid || (item.source != GearSource.RAID && item.source != GearSource.SET)
    }

    private fun averageStats(snapshots: List<GearSnapshot>): Map<StatType, Int> {
        if (snapshots.isEmpty()) {
            return emptyMap()
        }
        return snapshots.flatMap { it.stats.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> (values.sum().toDouble() / snapshots.size).roundToInt() }
    }
}
