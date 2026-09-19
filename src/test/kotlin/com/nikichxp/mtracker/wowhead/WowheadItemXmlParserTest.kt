package com.nikichxp.mtracker.wowhead

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WowheadItemXmlParserTest {

    private fun fixture(itemId: Int): String =
        javaClass.getResource("/stubs/wowhead/item-$itemId.xml")!!.readText()

    @Test
    fun `crafted item`() {
        val item = WowheadItemXmlParser.parse(240949, fixture(240949))!!
        assertThat(item.name).isEqualTo("Masterwork Sin'dorei Band")
        assertThat(item.quality).isEqualTo(4)
        assertThat(item.inventorySlot).isEqualTo("Finger")
        assertThat(item.sourceIds).containsExactly(1)
        assertThat(item.droppedBy).isNull()
        assertThat(item.icon).isNotBlank()
    }

    @Test
    fun `raid drop`() {
        val item = WowheadItemXmlParser.parse(268215, fixture(268215))!!
        assertThat(item.name).isEqualTo("Abyssal Broodfiend's Bardiche")
        assertThat(item.sourceIds).containsExactly(2)
        assertThat(item.droppedBy).isEqualTo("Ula'tek")
        assertThat(item.zoneId).isEqualTo(16915)
    }

    @Test
    fun `dungeon drop`() {
        val item = WowheadItemXmlParser.parse(251136, fixture(251136))!!
        assertThat(item.sourceIds).containsExactly(2, 4)
        assertThat(item.droppedBy).isEqualTo("Xathuux the Annihilator")
        assertThat(item.zoneId).isEqualTo(16091)
    }

    @Test
    fun `tier set item has no source data`() {
        val item = WowheadItemXmlParser.parse(271517, fixture(271517))!!
        assertThat(item.name).isNotBlank()
        assertThat(item.sourceIds).isEmpty()
        assertThat(item.droppedBy).isNull()
        assertThat(item.zoneId).isNull()
    }
}
