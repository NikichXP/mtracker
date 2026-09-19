package com.nikichxp.mtracker.wowhead

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.mtracker.domain.topgear.StatType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WowheadTooltipParserTest {

    private val objectMapper = ObjectMapper()

    private fun tooltip(itemId: Int): Pair<String?, String> {
        val node = objectMapper.readTree(javaClass.getResource("/stubs/wowhead/tooltip-$itemId.json")!!.readText())
        return node.get("name")?.asText() to node.get("tooltip").asText()
    }

    @Test
    fun `parses item level and stats with hybrid primary`() {
        val (name, html) = tooltip(251109)
        val dto = WowheadTooltipParser.parse(251109, name, html)
        assertThat(dto.itemLevel).isEqualTo(250)
        assertThat(dto.name).isEqualTo("Spellsnap Shadowmask")
        assertThat(dto.hybridPrimary).isEqualTo(
            HybridPrimaryStat(setOf(StatType.AGILITY, StatType.INTELLECT), 86)
        )
        assertThat(dto.stats).containsEntry(StatType.STAMINA, 1451)
            .containsEntry(StatType.CRITICAL_STRIKE, 52)
        assertThat(dto.stats[StatType.MASTERY]).isEqualTo(81)
        assertThat(dto.stats[StatType.ARMOR]).isEqualTo(88)
        assertThat(dto.gemStats).isEmpty()
    }

    @Test
    fun `parses gem stats by label`() {
        val (name, html) = tooltip(240906)
        val dto = WowheadTooltipParser.parse(240906, name, html)
        assertThat(dto.itemLevel).isEqualTo(295)
        assertThat(dto.gemStats).containsEntry(StatType.CRITICAL_STRIKE, 16)
            .containsEntry(StatType.HASTE, 7)
    }
}
