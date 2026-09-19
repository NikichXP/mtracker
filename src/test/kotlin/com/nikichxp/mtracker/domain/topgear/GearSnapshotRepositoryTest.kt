package com.nikichxp.mtracker.domain.topgear

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GearSnapshotRepositoryTest @Autowired constructor(
    private val snapshotRepository: GearSnapshotRepository,
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
}
