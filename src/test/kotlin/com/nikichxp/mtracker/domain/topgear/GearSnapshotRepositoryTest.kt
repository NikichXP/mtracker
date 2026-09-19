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
class GearSnapshotRepositoryTest @Autowired constructor(
    private val snapshotRepository: GearSnapshotRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `specOverview returns empty list when no snapshots exist`() {
        assertThat(snapshotRepository.specOverview()).isEmpty()
    }

    @Test
    fun `specOverview groups snapshots by spec`() {
        snapshotRepository.saveAll(
            listOf(
                snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001),
                snapshot(specId = 269, characterKey = "Jaina-gordunni", keystoneRunId = 9002),
                snapshot(specId = 251, specName = "Frost", specSlug = "frost", className = "Death Knight",
                    characterKey = "Arthas-gordunni", keystoneRunId = 9003),
            )
        )

        val rows = snapshotRepository.specOverview()

        assertThat(rows).hasSize(2)
        val windwalker = rows.single { it.specId == 269 }
        assertThat(windwalker.specName).isEqualTo("Windwalker")
        assertThat(windwalker.specSlug).isEqualTo("windwalker")
        assertThat(windwalker.className).isEqualTo("Monk")
        assertThat(windwalker.role).isEqualTo("dps")
        assertThat(windwalker.parseCount).isEqualTo(2)
        assertThat(windwalker.characterCount).isEqualTo(2)
        val frost = rows.single { it.specId == 251 }
        assertThat(frost.parseCount).isEqualTo(1)
        assertThat(frost.characterCount).isEqualTo(1)
    }

    @Test
    fun `specOverview counts distinct characters across multiple runs`() {
        snapshotRepository.saveAll(
            listOf(
                snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001),
                snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9002),
                snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9003),
            )
        )

        val row = snapshotRepository.specOverview().single()

        assertThat(row.parseCount).isEqualTo(3)
        assertThat(row.characterCount).isEqualTo(1)
    }

    @Test
    fun `specOverview averages equipped item level`() {
        snapshotRepository.saveAll(
            listOf(
                snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001, itemLevel = 250.0),
                snapshot(specId = 269, characterKey = "Jaina-gordunni", keystoneRunId = 9002, itemLevel = 254.0),
            )
        )

        val row = snapshotRepository.specOverview().single()

        assertThat(row.avgItemLevel).isEqualTo(252.0)
    }

    @Test
    fun `specOverview returns null average when item level is not recorded`() {
        snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001, itemLevel = null)
        )

        val row = snapshotRepository.specOverview().single()

        assertThat(row.avgItemLevel).isNull()
    }

    @Test
    fun `save and read back preserves all fields and collections`() {
        val saved = snapshotRepository.save(fullSnapshot())
        em.flush()
        em.clear()

        val loaded = snapshotRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.region).isEqualTo("eu")
        assertThat(loaded.season).isEqualTo("season-mn-2")
        assertThat(loaded.keystoneRunId).isEqualTo(9001)
        assertThat(loaded.characterKey).isEqualTo("Illidan-ravencrest")
        assertThat(loaded.name).isEqualTo("Illidan")
        assertThat(loaded.realmSlug).isEqualTo("ravencrest")
        assertThat(loaded.className).isEqualTo("Monk")
        assertThat(loaded.specId).isEqualTo(269)
        assertThat(loaded.specName).isEqualTo("Windwalker")
        assertThat(loaded.specSlug).isEqualTo("windwalker")
        assertThat(loaded.role).isEqualTo("dps")
        assertThat(loaded.dungeonName).isEqualTo("Den of Nalorakk")
        assertThat(loaded.dungeonShortName).isEqualTo("DON")
        assertThat(loaded.mythicLevel).isEqualTo(22)
        assertThat(loaded.runScore).isEqualTo(320.5)
        assertThat(loaded.timed).isTrue()
        assertThat(loaded.numKeystoneUpgrades).isEqualTo(2)
        assertThat(loaded.completedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
        assertThat(loaded.itemLevelEquipped).isEqualTo(250.0)
        assertThat(loaded.rioScore).isEqualTo(3123.4)
        assertThat(loaded.talentImportString).isEqualTo("talent-string")
        assertThat(loaded.capturedAt).isEqualTo(Instant.parse("2025-03-02T12:00:00Z"))

        assertThat(loaded.items).hasSize(1)
        val item = loaded.items[0]
        assertThat(item.id).isNotNull()
        assertThat(item.slot).isEqualTo("HEAD")
        assertThat(item.itemId).isEqualTo(240949)
        assertThat(item.itemLevel).isEqualTo(269)
        assertThat(item.itemName).isEqualTo("Crown of the Fallen")
        assertThat(item.itemQuality).isEqualTo(4)
        assertThat(item.tierSetId).isEqualTo("tier-1")
        assertThat(item.source).isEqualTo(GearSource.RAID)
        assertThat(item.bonusIds).containsExactlyInAnyOrder(1234, 5678)
        assertThat(item.gems).containsExactly(GearSocket(gemId = 213743, name = "Quick Onyx"))
        assertThat(item.enchants).containsExactly(GearEnchantment(enchantId = 7654, name = "Enchant"))

        assertThat(loaded.partySpecs)
            .containsExactly(PartyMemberSpec(269, "Windwalker", "windwalker", "dps", "Monk"))
        assertThat(loaded.stats).isEqualTo(mapOf(StatType.HASTE to 1234, StatType.MASTERY to 567))
        assertThat(loaded.gearSources).containsExactlyInAnyOrder(GearSource.RAID, GearSource.KEYS)
    }

    @Test
    fun `stats map is stored with enum key and stat_value column`() {
        val saved = snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001).apply {
                stats = mapOf(StatType.HASTE to 42)
            }
        )
        em.flush()

        val rows = em.entityManager
            .createNativeQuery("select stat, stat_value from gear_snapshot_stats where snapshot_id = :id")
            .setParameter("id", saved.id)
            .resultList

        assertThat(rows).hasSize(1)
        val row = rows.single() as Array<*>
        assertThat(row[0]).isEqualTo("HASTE")
        assertThat((row[1] as Number).toInt()).isEqualTo(42)
    }

    @Test
    fun `nullable fields persist as null`() {
        val saved = snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001, itemLevel = null)
        )
        em.flush()
        em.clear()

        val loaded = snapshotRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.dungeonShortName).isNull()
        assertThat(loaded.runScore).isNull()
        assertThat(loaded.completedAt).isNull()
        assertThat(loaded.itemLevelEquipped).isNull()
        assertThat(loaded.rioScore).isNull()
        assertThat(loaded.talentImportString).isNull()
        assertThat(loaded.items).isEmpty()
        assertThat(loaded.partySpecs).isEmpty()
        assertThat(loaded.stats).isEmpty()
        assertThat(loaded.gearSources).isEmpty()
    }

    @Test
    fun `multiple items per snapshot round-trip`() {
        val snapshot = snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001)
        snapshot.items = listOf(
            GearSnapshotItem(snapshot = snapshot, slot = "HEAD", itemId = 240949, itemLevel = 269),
            GearSnapshotItem(snapshot = snapshot, slot = "FEET", itemId = 251136, itemLevel = 262,
                bonusIds = listOf(42)),
        )
        val saved = snapshotRepository.save(snapshot)
        em.flush()
        em.clear()

        val loaded = snapshotRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.items).hasSize(2)
        assertThat(loaded.items.map { it.slot }).containsExactlyInAnyOrder("HEAD", "FEET")
        assertThat(loaded.items.single { it.slot == "FEET" }.bonusIds).containsExactly(42)
    }

    @Test
    fun `adding item to managed collection cascade-persists it`() {
        val saved = snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001)
        )
        em.flush()
        em.clear()

        val loaded = snapshotRepository.findById(saved.id!!).orElseThrow()
        val newItem = GearSnapshotItem(snapshot = loaded, slot = "RING", itemId = 268215, itemLevel = 265)
        (loaded.items as MutableList<GearSnapshotItem>).add(newItem)
        em.flush()
        em.clear()

        val reloaded = snapshotRepository.findById(saved.id!!).orElseThrow()
        assertThat(reloaded.items.map { it.slot }).containsExactly("RING")
        assertThat(reloaded.items[0].id).isNotNull()
    }

    @Test
    fun `orphan removal deletes items`() {
        val saved = snapshotRepository.save(fullSnapshot())
        em.flush()
        em.clear()

        val loaded = snapshotRepository.findById(saved.id!!).orElseThrow()
        // orphanRemoval forbids replacing the collection reference; mutate the persistent bag instead
        (loaded.items as MutableList<GearSnapshotItem>).clear()
        em.flush()
        em.clear()

        val remaining = em.entityManager
            .createQuery("select count(i) from GearSnapshotItem i", Long::class.java)
            .singleResult
        assertThat(remaining).isEqualTo(0L)
        assertThat(snapshotRepository.findById(saved.id!!).orElseThrow().items).isEmpty()
    }

    @Test
    fun `season keystoneRunId and characterKey triple is unique`() {
        snapshotRepository.save(snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001))
        em.flush()

        assertThatThrownBy {
            snapshotRepository.saveAndFlush(
                snapshot(specId = 251, characterKey = "Illidan-ravencrest", keystoneRunId = 9001)
            )
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `existsBySeasonAndKeystoneRunIdAndCharacterKey`() {
        snapshotRepository.save(snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001))

        assertThat(
            snapshotRepository.existsBySeasonAndKeystoneRunIdAndCharacterKey(
                "season-mn-2", 9001, "Illidan-ravencrest"
            )
        ).isTrue()
        assertThat(
            snapshotRepository.existsBySeasonAndKeystoneRunIdAndCharacterKey(
                "season-mn-2", 9001, "Jaina-gordunni"
            )
        ).isFalse()
        assertThat(
            snapshotRepository.existsBySeasonAndKeystoneRunIdAndCharacterKey(
                "season-mn-2", 9002, "Illidan-ravencrest"
            )
        ).isFalse()
    }

    @Test
    fun `findIdsByCapturedAtBefore returns ids of old snapshots`() {
        val old = snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001)
                .apply { capturedAt = Instant.parse("2025-01-01T00:00:00Z") }
        )
        snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Jaina-gordunni", keystoneRunId = 9002)
                .apply { capturedAt = Instant.parse("2025-03-01T00:00:00Z") }
        )
        em.flush()

        val ids = snapshotRepository.findIdsByCapturedAtBefore(Instant.parse("2025-02-01T00:00:00Z"))

        assertThat(ids).containsExactly(old.id)
    }

    @Test
    fun `findIdsByCapturedAtBefore excludes snapshot captured exactly at cutoff`() {
        val cutoff = Instant.parse("2025-02-01T00:00:00Z")
        snapshotRepository.save(
            snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001)
                .apply { capturedAt = cutoff }
        )
        em.flush()

        assertThat(snapshotRepository.findIdsByCapturedAtBefore(cutoff)).isEmpty()
    }

    @Test
    fun `findBySpecId returns matching snapshots`() {
        snapshotRepository.save(snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001))
        snapshotRepository.save(snapshot(specId = 269, characterKey = "Jaina-gordunni", keystoneRunId = 9002))
        snapshotRepository.save(snapshot(specId = 251, characterKey = "Arthas-gordunni", keystoneRunId = 9003))

        val found = snapshotRepository.findBySpecId(269)

        assertThat(found.map { it.characterKey })
            .containsExactlyInAnyOrder("Illidan-ravencrest", "Jaina-gordunni")
    }

    @Test
    fun `update persists changes`() {
        val saved = snapshotRepository.save(fullSnapshot())
        em.flush()
        em.clear()

        val loaded = snapshotRepository.findById(saved.id!!).orElseThrow()
        loaded.rioScore = 3500.0
        loaded.stats = mapOf(StatType.VERSATILITY to 42)
        em.flush()
        em.clear()

        val updated = snapshotRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.rioScore).isEqualTo(3500.0)
        assertThat(updated.stats).isEqualTo(mapOf(StatType.VERSATILITY to 42))
    }

    @Test
    fun `delete removes snapshot and cascades to items`() {
        val saved = snapshotRepository.save(fullSnapshot())
        em.flush()

        snapshotRepository.delete(saved)
        em.flush()

        assertThat(snapshotRepository.findById(saved.id!!)).isEmpty
        val remaining = em.entityManager
            .createQuery("select count(i) from GearSnapshotItem i", Long::class.java)
            .singleResult
        assertThat(remaining).isEqualTo(0L)
    }

    private fun snapshot(
        specId: Int,
        characterKey: String,
        keystoneRunId: Long,
        specName: String = "Windwalker",
        specSlug: String = "windwalker",
        className: String = "Monk",
        itemLevel: Double? = 250.0,
    ) = GearSnapshot(
        region = "eu",
        season = "season-mn-2",
        keystoneRunId = keystoneRunId,
        characterKey = characterKey,
        name = characterKey.substringBefore("-"),
        realmSlug = characterKey.substringAfter("-"),
        className = className,
        specId = specId,
        specName = specName,
        specSlug = specSlug,
        role = "dps",
        dungeonName = "Den of Nalorakk",
        mythicLevel = 22,
        itemLevelEquipped = itemLevel,
    )

    private fun fullSnapshot(): GearSnapshot {
        val snapshot = snapshot(specId = 269, characterKey = "Illidan-ravencrest", keystoneRunId = 9001).apply {
            dungeonShortName = "DON"
            runScore = 320.5
            timed = true
            numKeystoneUpgrades = 2
            completedAt = Instant.parse("2025-03-01T12:00:00Z")
            rioScore = 3123.4
            talentImportString = "talent-string"
            capturedAt = Instant.parse("2025-03-02T12:00:00Z")
            partySpecs = listOf(PartyMemberSpec(269, "Windwalker", "windwalker", "dps", "Monk"))
            stats = mapOf(StatType.HASTE to 1234, StatType.MASTERY to 567)
            gearSources = setOf(GearSource.RAID, GearSource.KEYS)
        }
        snapshot.items = listOf(
            GearSnapshotItem(
                snapshot = snapshot,
                slot = "HEAD",
                itemId = 240949,
                itemLevel = 269,
                itemName = "Crown of the Fallen",
                itemQuality = 4,
                tierSetId = "tier-1",
                source = GearSource.RAID,
                bonusIds = listOf(1234, 5678),
                gems = listOf(GearSocket(gemId = 213743, name = "Quick Onyx")),
                enchants = listOf(GearEnchantment(enchantId = 7654, name = "Enchant")),
            )
        )
        return snapshot
    }
}
