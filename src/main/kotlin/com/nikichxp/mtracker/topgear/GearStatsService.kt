package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.domain.topgear.GearItemStats
import com.nikichxp.mtracker.domain.topgear.GearItemStatsRepository
import com.nikichxp.mtracker.domain.topgear.StatType
import com.nikichxp.mtracker.raiderio.dto.GearItemDto
import com.nikichxp.mtracker.wowhead.IWowheadService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GearStatsService(
    private val repository: GearItemStatsRepository,
    private val wowheadService: IWowheadService,
    private val primaryStatResolver: PrimaryStatResolver,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun statsFor(gearItem: GearItemDto, primaryStat: StatType): Map<StatType, Int> {
        val bonusKey = gearItem.bonuses.sorted().joinToString(":")
        repository.findByItemIdAndBonusKey(gearItem.itemId, bonusKey)?.let { return it.stats }
        val tooltip = wowheadService.fetchItemTooltip(gearItem.itemId, gearItem.bonuses) ?: return emptyMap()
        val stats = tooltip.stats.toMutableMap()
        tooltip.hybridPrimary?.let { hybrid ->
            val resolved =
                if (primaryStat in hybrid.options) primaryStat else primaryStatResolver.hybridFallback(hybrid.options)
            stats[resolved] = hybrid.value
        }
        val entity = GearItemStats(
            itemId = gearItem.itemId,
            bonusKey = bonusKey,
            itemLevel = tooltip.itemLevel,
            stats = stats,
            fetchedAt = Instant.now(),
        )
        return try {
            repository.save(entity).stats
        } catch (e: DataIntegrityViolationException) {
            repository.findByItemIdAndBonusKey(gearItem.itemId, bonusKey)?.stats ?: stats
        }
    }

    suspend fun gemStatsFor(gemIds: List<Int>): Map<StatType, Int> {
        val totals = mutableMapOf<StatType, Int>()
        for (gemId in gemIds.distinct()) {
            val stats = repository.findByItemIdAndBonusKey(gemId, "")?.stats ?: run {
                val tooltip = wowheadService.fetchItemTooltip(gemId, emptyList())
                if (tooltip == null) {
                    log.warn("No wowhead tooltip for gem {}", gemId)
                    emptyMap()
                } else {
                    val entity = GearItemStats(
                        itemId = gemId,
                        bonusKey = "",
                        itemLevel = tooltip.itemLevel,
                        stats = tooltip.gemStats,
                        fetchedAt = Instant.now(),
                    )
                    try {
                        repository.save(entity).stats
                    } catch (e: DataIntegrityViolationException) {
                        repository.findByItemIdAndBonusKey(gemId, "")?.stats ?: tooltip.gemStats
                    }
                }
            }
            stats.forEach { (stat, value) -> totals.merge(stat, value, Int::plus) }
        }
        return totals
    }
}
