package com.nikichxp.mtracker.domain.weekly

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
class WeeklySnapshotRepositoryTest @Autowired constructor(
    private val weeklySnapshotRepository: WeeklySnapshotRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = weeklySnapshotRepository.save(snapshot())
        em.flush()
        em.clear()

        val loaded = weeklySnapshotRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.weekKey).isEqualTo("2025-W10")
        assertThat(loaded.characterKey).isEqualTo("Illidan-ravencrest")
        assertThat(loaded.mythicPlusScore).isEqualTo(3123.4)
        assertThat(loaded.weeklyRunsCount).isEqualTo(8)
        assertThat(loaded.weeklyHighestLevel).isEqualTo(22)
        assertThat(loaded.capturedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
    }

    @Test
    fun `weekKey and characterKey pair is unique`() {
        weeklySnapshotRepository.save(snapshot())
        em.flush()

        assertThatThrownBy {
            weeklySnapshotRepository.saveAndFlush(snapshot(mythicPlusScore = 1.0))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `same weekKey is allowed for a different character`() {
        weeklySnapshotRepository.save(snapshot())
        val other = weeklySnapshotRepository.saveAndFlush(snapshot(characterKey = "Jaina-gordunni"))

        assertThat(other.id).isNotNull()
    }

    @Test
    fun `findDistinctWeekKeys returns distinct keys ordered desc`() {
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W10"))
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W12"))
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W11"))
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W12", characterKey = "Jaina-gordunni"))

        assertThat(weeklySnapshotRepository.findDistinctWeekKeys())
            .containsExactly("2025-W12", "2025-W11", "2025-W10")
    }

    @Test
    fun `findByCharacterKeyOrderByCapturedAtDesc returns newest first`() {
        weeklySnapshotRepository.save(
            snapshot(weekKey = "2025-W10", capturedAt = Instant.parse("2025-03-01T00:00:00Z"))
        )
        weeklySnapshotRepository.save(
            snapshot(weekKey = "2025-W11", capturedAt = Instant.parse("2025-03-08T00:00:00Z"))
        )
        weeklySnapshotRepository.save(
            snapshot(weekKey = "2025-W12", capturedAt = Instant.parse("2025-03-05T00:00:00Z"))
        )
        weeklySnapshotRepository.save(
            snapshot(weekKey = "2025-W10", characterKey = "Jaina-gordunni",
                capturedAt = Instant.parse("2025-03-09T00:00:00Z"))
        )

        val found = weeklySnapshotRepository.findByCharacterKeyOrderByCapturedAtDesc("Illidan-ravencrest")

        assertThat(found.map { it.weekKey }).containsExactly("2025-W11", "2025-W12", "2025-W10")
    }

    @Test
    fun `findByWeekKey returns snapshots of that week`() {
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W10"))
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W10", characterKey = "Jaina-gordunni"))
        weeklySnapshotRepository.save(snapshot(weekKey = "2025-W11"))

        val found = weeklySnapshotRepository.findByWeekKey("2025-W10")

        assertThat(found.map { it.characterKey })
            .containsExactlyInAnyOrder("Illidan-ravencrest", "Jaina-gordunni")
    }

    @Test
    fun `findByWeekKeyAndCharacterKey returns single snapshot`() {
        weeklySnapshotRepository.save(snapshot())

        val found = weeklySnapshotRepository.findByWeekKeyAndCharacterKey("2025-W10", "Illidan-ravencrest")

        assertThat(found).isNotNull()
        assertThat(found!!.weeklyRunsCount).isEqualTo(8)
        assertThat(weeklySnapshotRepository.findByWeekKeyAndCharacterKey("2025-W10", "missing")).isNull()
    }

    @Test
    fun `nullable mythicPlusScore persists as null`() {
        val saved = weeklySnapshotRepository.save(snapshot(mythicPlusScore = null))
        em.flush()
        em.clear()

        assertThat(weeklySnapshotRepository.findById(saved.id!!).orElseThrow().mythicPlusScore).isNull()
    }

    @Test
    fun `update persists changes`() {
        val saved = weeklySnapshotRepository.save(snapshot())
        em.flush()
        em.clear()

        val loaded = weeklySnapshotRepository.findById(saved.id!!).orElseThrow()
        loaded.mythicPlusScore = 3500.0
        loaded.weeklyRunsCount = 12
        weeklySnapshotRepository.saveAndFlush(loaded)
        em.clear()

        val updated = weeklySnapshotRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.mythicPlusScore).isEqualTo(3500.0)
        assertThat(updated.weeklyRunsCount).isEqualTo(12)
    }

    @Test
    fun `delete removes snapshot`() {
        val saved = weeklySnapshotRepository.save(snapshot())
        em.flush()

        weeklySnapshotRepository.delete(saved)
        em.flush()

        assertThat(weeklySnapshotRepository.findById(saved.id!!)).isEmpty
    }

    private fun snapshot(
        weekKey: String = "2025-W10",
        characterKey: String = "Illidan-ravencrest",
        mythicPlusScore: Double? = 3123.4,
        capturedAt: Instant = Instant.parse("2025-03-01T12:00:00Z"),
    ) = WeeklySnapshot(
        weekKey = weekKey,
        characterKey = characterKey,
        mythicPlusScore = mythicPlusScore,
        weeklyRunsCount = 8,
        weeklyHighestLevel = 22,
        capturedAt = capturedAt,
    )
}
