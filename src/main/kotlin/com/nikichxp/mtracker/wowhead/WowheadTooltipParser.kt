package com.nikichxp.mtracker.wowhead

import com.nikichxp.mtracker.domain.topgear.StatType

object WowheadTooltipParser {

    private val ilvlRegex = Regex("""Item Level <!--ilvl-->\s*([\d,]+)""")
    private val statRegex = Regex("""<!--(?:stat|rtg|amr)\d*-->\s*\+?\s*([\d,]+)\s*([^\n<]+?)(?=<)""")
    private val gemStatRegex = Regex("""<!--gem\d+-->\s*([\d,]+)\s*<!---->\s*([A-Za-z][A-Za-z ]*?)(?=\s*(?:&|<|$))""")
    private val tagRegex = Regex("""<![^>]*>""")

    private val statLabels = mapOf(
        "Strength" to StatType.STRENGTH,
        "Agility" to StatType.AGILITY,
        "Intellect" to StatType.INTELLECT,
        "Stamina" to StatType.STAMINA,
        "Critical Strike" to StatType.CRITICAL_STRIKE,
        "Haste" to StatType.HASTE,
        "Versatility" to StatType.VERSATILITY,
        "Mastery" to StatType.MASTERY,
        "Speed" to StatType.SPEED,
        "Leech" to StatType.LEECH,
        "Avoidance" to StatType.AVOIDANCE,
        "Armor" to StatType.ARMOR,
    )

    fun parse(itemId: Int, name: String?, tooltipHtml: String): WowheadTooltipDto {
        val stats = mutableMapOf<StatType, Int>()
        var hybrid: HybridPrimaryStat? = null
        for (match in statRegex.findAll(tooltipHtml)) {
            val value = match.groupValues[1].replace(",", "").toIntOrNull() ?: continue
            val label = tagRegex.replace(match.groupValues[2], "").trim()
            if (label.startsWith("[") && label.endsWith("]")) {
                val options = label.removePrefix("[").removeSuffix("]")
                    .split(" or ")
                    .mapNotNull { statLabels[it.trim()] }
                    .toSet()
                if (options.isNotEmpty()) {
                    hybrid = HybridPrimaryStat(options, value)
                }
            } else {
                statLabels[label]?.let { stats[it] = value }
            }
        }
        val gemStats = mutableMapOf<StatType, Int>()
        for (match in gemStatRegex.findAll(tooltipHtml)) {
            val value = match.groupValues[1].replace(",", "").toIntOrNull() ?: continue
            statLabels[match.groupValues[2].trim()]?.let { gemStats.merge(it, value, Int::plus) }
        }
        return WowheadTooltipDto(
            itemId = itemId,
            name = name,
            itemLevel = ilvlRegex.find(tooltipHtml)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull(),
            stats = stats,
            hybridPrimary = hybrid,
            gemStats = gemStats,
        )
    }
}
