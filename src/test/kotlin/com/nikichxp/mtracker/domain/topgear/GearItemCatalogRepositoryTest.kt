package com.nikichxp.mtracker.domain.topgear

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GearItemCatalogRepositoryTest @Autowired constructor(
    private val gearItemCatalogRepository: GearItemCatalogRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        gearItemCatalogRepository.save(item())
        em.flush()
        em.clear()

        val loaded = gearItemCatalogRepository.findById(240949).orElseThrow()

        assertThat(loaded.itemId).isEqualTo(240949)
        assertThat(loaded.englishName).isEqualTo("Crown of the Fallen")
        assertThat(loaded.quality).isEqualTo(4)
        assertThat(loaded.icon).isEqualTo("inv_helm_plate_raidpaladin")
        assertThat(loaded.inventorySlot).isEqualTo("HEAD")
        assertThat(loaded.source).isEqualTo(GearSource.RAID)
        assertThat(loaded.sourceDetail).isEqualTo("Den of Nalorakk")
        assertThat(loaded.wowheadSourceIds).containsExactlyInAnyOrder(11, 22, 33)
        assertThat(loaded.fetchedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
        assertThat(loaded.version).isEqualTo(0L)
    }

    @Test
    fun `itemId is assigned not generated`() {
        val saved = gearItemCatalogRepository.save(item())

        assertThat(saved.itemId).isEqualTo(240949)
        assertThat(gearItemCatalogRepository.existsById(240949)).isTrue()
    }

    @Test
    fun `inserting a second entity with an existing itemId fails`() {
        gearItemCatalogRepository.save(item())
        em.flush()

        assertThatThrownBy {
            gearItemCatalogRepository.saveAndFlush(item().apply { englishName = "Duplicate" })
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `stale update fails with optimistic locking`() {
        val saved = gearItemCatalogRepository.saveAndFlush(item())
        em.clear()

        val stale = gearItemCatalogRepository.findById(saved.itemId).orElseThrow()
        em.clear()

        val fresh = gearItemCatalogRepository.findById(saved.itemId).orElseThrow()
        fresh.englishName = "Fresh Name"
        gearItemCatalogRepository.saveAndFlush(fresh)

        stale.englishName = "Stale Name"
        assertThatThrownBy {
            gearItemCatalogRepository.saveAndFlush(stale)
        }.isInstanceOf(OptimisticLockingFailureException::class.java)
    }

    @Test
    fun `version increments on update`() {
        gearItemCatalogRepository.save(item())
        em.flush()
        em.clear()

        val loaded = gearItemCatalogRepository.findById(240949).orElseThrow()
        assertThat(loaded.version).isEqualTo(0L)

        loaded.englishName = "Renamed Crown"
        gearItemCatalogRepository.saveAndFlush(loaded)
        em.clear()

        val updated = gearItemCatalogRepository.findById(240949).orElseThrow()
        assertThat(updated.version).isEqualTo(1L)
        assertThat(updated.englishName).isEqualTo("Renamed Crown")
    }

    @Test
    fun `update persists changes`() {
        gearItemCatalogRepository.save(item())
        em.flush()
        em.clear()

        val loaded = gearItemCatalogRepository.findById(240949).orElseThrow()
        loaded.source = GearSource.KEYS
        loaded.wowheadSourceIds = listOf(99)
        em.flush()
        em.clear()

        val updated = gearItemCatalogRepository.findById(240949).orElseThrow()
        assertThat(updated.source).isEqualTo(GearSource.KEYS)
        assertThat(updated.wowheadSourceIds).containsExactly(99)
    }

    @Test
    fun `delete removes catalog item`() {
        gearItemCatalogRepository.save(item())
        em.flush()

        gearItemCatalogRepository.deleteById(240949)
        em.flush()

        assertThat(gearItemCatalogRepository.findById(240949)).isEmpty
    }

    private fun item(itemId: Int = 240949) = GearItemCatalog(
        itemId = itemId,
        englishName = "Crown of the Fallen",
        quality = 4,
        icon = "inv_helm_plate_raidpaladin",
        inventorySlot = "HEAD",
        source = GearSource.RAID,
        sourceDetail = "Den of Nalorakk",
        wowheadSourceIds = listOf(11, 22, 33),
        fetchedAt = Instant.parse("2025-03-01T12:00:00Z"),
    )
}
