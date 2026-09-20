package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.topgear.GearItemCatalog
import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.GearItemDto
import com.nikichxp.mtracker.raiderio.dto.NamedSlugDto
import com.nikichxp.mtracker.raiderio.dto.RaidingStaticDataDto
import com.nikichxp.mtracker.raiderio.dto.StaticRaidDto
import com.nikichxp.mtracker.wowhead.WowheadItemDto
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GearSourceResolverTest {

    private val raiderIoService: IRaiderIoService = mock()
    private val resolver = GearSourceResolver(
        RaidEncounterCatalog(raiderIoService, MtrackerProperties())
    )

    private fun wowheadItem(
        sourceIds: List<Int> = emptyList(),
        droppedBy: String? = null,
        zoneId: Int? = null,
    ) = WowheadItemDto(1, "item", 4, "icon", "Head", sourceIds, droppedBy, zoneId)

    private fun raidEncounters() = RaidingStaticDataDto(
        listOf(StaticRaidDto(name = "Liberation of Undermine", encounters = listOf(NamedSlugDto(name = "Ula'tek"))))
    )

    @Test
    fun `tier piece resolves to SET over everything`() {
        val catalog = GearItemCatalog(itemId = 1, source = GearSource.RAID)
        val source = resolver.effectiveSource(catalog, GearItemDto(itemId = 1, tier = "36"))
        assertThat(source).isEqualTo(GearSource.SET)
    }

    @Test
    fun `effectiveSource passes through catalog source without tier`() {
        val catalog = GearItemCatalog(itemId = 1, source = GearSource.KEYS)
        assertThat(resolver.effectiveSource(catalog, GearItemDto(itemId = 1))).isEqualTo(GearSource.KEYS)
    }

    @Test
    fun `crafted source id wins`() = runBlocking<Unit> {
        val source = resolver.classify(wowheadItem(sourceIds = listOf(1)))
        assertThat(source).isEqualTo(GearSource.CRAFTED)
    }

    @Test
    fun `drop by raid encounter resolves to RAID`() = runBlocking<Unit> {
        whenever(raiderIoService.fetchRaidingStaticData(any())).thenReturn(raidEncounters())
        val source = resolver.classify(wowheadItem(sourceIds = listOf(2), droppedBy = "Ula'tek"))
        assertThat(source).isEqualTo(GearSource.RAID)
    }

    @Test
    fun `dungeon drop resolves to KEYS`() = runBlocking<Unit> {
        whenever(raiderIoService.fetchRaidingStaticData(any())).thenReturn(raidEncounters())
        val source = resolver.classify(
            wowheadItem(sourceIds = listOf(2, 4), droppedBy = "Xathuux the Annihilator")
        )
        assertThat(source).isEqualTo(GearSource.KEYS)
    }

    @Test
    fun `no source data resolves to UNKNOWN`() = runBlocking<Unit> {
        assertThat(resolver.classify(wowheadItem())).isEqualTo(GearSource.UNKNOWN)
    }

    @Test
    fun `raid source detail includes the raid instance name`() = runBlocking<Unit> {
        whenever(raiderIoService.fetchRaidingStaticData(any())).thenReturn(raidEncounters())
        val detail = resolver.sourceDetail(wowheadItem(droppedBy = "Ula'tek"), GearSource.RAID)
        assertThat(detail).isEqualTo("Ula'tek — Liberation of Undermine")
    }

    @Test
    fun `non-raid source detail keeps just the dropped-by text`() = runBlocking<Unit> {
        val detail = resolver.sourceDetail(
            wowheadItem(droppedBy = "Xathuux the Annihilator"),
            GearSource.KEYS,
        )
        assertThat(detail).isEqualTo("Xathuux the Annihilator")
    }
}
