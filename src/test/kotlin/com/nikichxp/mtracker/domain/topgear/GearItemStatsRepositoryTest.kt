package com.nikichxp.mtracker.domain.topgear

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GearItemStatsRepositoryTest @Autowired constructor(
    private val gearItemStatsRepository: GearItemStatsRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = gearItemStatsRepository.save(stats())
        em.flush()
        em.clear()

        val loaded = gearItemStatsRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.itemId).isEqualTo(240949)
        assertThat(loaded.bonusKey).isEqualTo("1234:5678")
        assertThat(loaded.itemLevel).isEqualTo(269)
        assertThat(loaded.stats).isEqualTo(
            mapOf(
                StatType.STRENGTH to 500,
                StatType.HASTE to 1234,
                StatType.MASTERY to 567,
            )
        )
        assertThat(loaded.fetchedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
    }

    @Test
    fun `stats map supports all StatType values`() {
        val allStats = StatType.entries.associateWith { it.ordinal * 100 }
        val saved = gearItemStatsRepository.save(stats().apply { stats = allStats })
        em.flush()
        em.clear()

        assertThat(gearItemStatsRepository.findById(saved.id!!).orElseThrow().stats)
            .isEqualTo(allStats)
    }

    @Test
    fun `nullable itemLevel persists as null`() {
        val saved = gearItemStatsRepository.save(stats(itemLevel = null))
        em.flush()
        em.clear()

        assertThat(gearItemStatsRepository.findById(saved.id!!).orElseThrow().itemLevel).isNull()
    }

    @Test
    fun `itemId and bonusKey pair is unique`() {
        gearItemStatsRepository.save(stats())
        em.flush()

        assertThatThrownBy {
            gearItemStatsRepository.saveAndFlush(stats(itemLevel = 270))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `same itemId is allowed with a different bonusKey`() {
        gearItemStatsRepository.save(stats())
        val other = gearItemStatsRepository.saveAndFlush(stats(bonusKey = "9999"))

        assertThat(other.id).isNotNull()
    }

    @Test
    fun `findByItemIdAndBonusKey returns matching stats`() {
        gearItemStatsRepository.save(stats())
        gearItemStatsRepository.save(stats(itemId = 240949, bonusKey = "9999"))
        gearItemStatsRepository.save(stats(itemId = 251136, bonusKey = "1234:5678"))

        val found = gearItemStatsRepository.findByItemIdAndBonusKey(240949, "1234:5678")

        assertThat(found).isNotNull()
        assertThat(found!!.itemLevel).isEqualTo(269)
        assertThat(gearItemStatsRepository.findByItemIdAndBonusKey(240949, "missing")).isNull()
    }

    @Test
    fun `update persists changes`() {
        val saved = gearItemStatsRepository.save(stats())
        em.flush()
        em.clear()

        val loaded = gearItemStatsRepository.findById(saved.id!!).orElseThrow()
        loaded.itemLevel = 280
        loaded.stats = mapOf(StatType.VERSATILITY to 42)
        em.flush()
        em.clear()

        val updated = gearItemStatsRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.itemLevel).isEqualTo(280)
        assertThat(updated.stats).isEqualTo(mapOf(StatType.VERSATILITY to 42))
    }

    @Test
    fun `delete removes stats`() {
        val saved = gearItemStatsRepository.save(stats())
        em.flush()

        gearItemStatsRepository.delete(saved)
        em.flush()

        assertThat(gearItemStatsRepository.findById(saved.id!!)).isEmpty
    }

    private fun stats(
        itemId: Int = 240949,
        bonusKey: String = "1234:5678",
        itemLevel: Int? = 269,
    ) = GearItemStats(
        itemId = itemId,
        bonusKey = bonusKey,
        itemLevel = itemLevel,
        stats = mapOf(
            StatType.STRENGTH to 500,
            StatType.HASTE to 1234,
            StatType.MASTERY to 567,
        ),
        fetchedAt = Instant.parse("2025-03-01T12:00:00Z"),
    )
}
