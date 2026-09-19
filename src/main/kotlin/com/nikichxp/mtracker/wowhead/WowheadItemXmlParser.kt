package com.nikichxp.mtracker.wowhead

object WowheadItemXmlParser {

    private val nameRegex = Regex("""<name><!\[CDATA\[(.*?)]]></name>""")
    private val qualityRegex = Regex("""<quality id="(\d+)"""")
    private val iconRegex = Regex("""<icon[^>]*>([^<]+)</icon>""")
    private val slotRegex = Regex("""<inventorySlot id="\d+">([^<]+)</inventorySlot>""")
    private val sourceRegex = Regex(""""source"\s*:\s*\[([0-9,\s]*)]""")
    private val sourceMoreRegex = Regex(""""sourcemore"\s*:\s*\[(.*?)]]>""", RegexOption.DOT_MATCHES_ALL)
    private val zoneRegex = Regex(""""z"\s*:\s*(\d+)""")
    private val droppedByRegex = Regex("""whtt-droppedby">Dropped by:\s*([^<]+)""")

    fun parse(itemId: Int, xml: String): WowheadItemDto? {
        if (!xml.contains("<item")) {
            return null
        }
        val sourceIds = sourceRegex.find(xml)?.groupValues?.get(1)
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
        val sourceMore = sourceMoreRegex.find(xml)?.groupValues?.get(1)
        return WowheadItemDto(
            itemId = itemId,
            name = nameRegex.find(xml)?.groupValues?.get(1),
            quality = qualityRegex.find(xml)?.groupValues?.get(1)?.toIntOrNull(),
            icon = iconRegex.find(xml)?.groupValues?.get(1),
            inventorySlot = slotRegex.find(xml)?.groupValues?.get(1),
            sourceIds = sourceIds,
            droppedBy = droppedByRegex.find(xml)?.groupValues?.get(1)?.trim(),
            zoneId = sourceMore?.let { zoneRegex.find(it)?.groupValues?.get(1)?.toIntOrNull() },
        )
    }
}
