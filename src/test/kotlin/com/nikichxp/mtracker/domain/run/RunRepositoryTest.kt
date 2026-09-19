package com.nikichxp.mtracker.domain.run

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
class RunRepositoryTest @Autowired constructor(
    private val runRepository: RunRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = runRepository.save(run())
        em.flush()
        em.clear()

        val loaded = runRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.season).isEqualTo("season-mn-2")
        assertThat(loaded.keystoneRunId).isEqualTo(9001)
        assertThat(loaded.dungeonName).isEqualTo("Den of Nalorakk")
        assertThat(loaded.dungeonShortName).isEqualTo("DON")
        assertThat(loaded.mythicLevel).isEqualTo(22)
        assertThat(loaded.score).isEqualTo(320.5)
        assertThat(loaded.timed).isTrue()
        assertThat(loaded.numKeystoneUpgrades).isEqualTo(2)
        assertThat(loaded.clearTimeMs).isEqualTo(1_234_567L)
        assertThat(loaded.keystoneTimeMs).isEqualTo(1_800_000L)
        assertThat(loaded.completedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
        assertThat(loaded.url).isEqualTo("https://raider.io/runs/9001")
    }

    @Test
    fun `season and keystoneRunId pair is unique`() {
        runRepository.save(run())
        em.flush()

        assertThatThrownBy {
            runRepository.saveAndFlush(run(dungeonName = "Other Dungeon"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `same keystoneRunId is allowed in a different season`() {
        runRepository.save(run())
        val other = runRepository.saveAndFlush(run(season = "season-tww-1"))

        assertThat(other.id).isNotNull()
    }

    @Test
    fun `findBySeasonAndKeystoneRunId returns matching run`() {
        runRepository.save(run())
        runRepository.save(run(season = "season-tww-1"))

        val found = runRepository.findBySeasonAndKeystoneRunId("season-mn-2", 9001)

        assertThat(found).isNotNull()
        assertThat(found!!.dungeonName).isEqualTo("Den of Nalorakk")
        assertThat(runRepository.findBySeasonAndKeystoneRunId("season-mn-2", 9999)).isNull()
    }

    @Test
    fun `existsBySeasonAndKeystoneRunId`() {
        runRepository.save(run())

        assertThat(runRepository.existsBySeasonAndKeystoneRunId("season-mn-2", 9001)).isTrue()
        assertThat(runRepository.existsBySeasonAndKeystoneRunId("season-mn-2", 9002)).isFalse()
        assertThat(runRepository.existsBySeasonAndKeystoneRunId("season-tww-1", 9001)).isFalse()
    }

    @Test
    fun `nullable fields persist as null`() {
        val saved = runRepository.save(
            Run(season = "season-mn-2", keystoneRunId = 9001, dungeonName = "Den", mythicLevel = 10)
        )
        em.flush()
        em.clear()

        val loaded = runRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.dungeonShortName).isNull()
        assertThat(loaded.score).isNull()
        assertThat(loaded.clearTimeMs).isNull()
        assertThat(loaded.keystoneTimeMs).isNull()
        assertThat(loaded.completedAt).isNull()
        assertThat(loaded.url).isNull()
        assertThat(loaded.timed).isFalse()
        assertThat(loaded.numKeystoneUpgrades).isEqualTo(0)
    }

    @Test
    fun `update persists changes`() {
        val saved = runRepository.save(run())
        em.flush()
        em.clear()

        val loaded = runRepository.findById(saved.id!!).orElseThrow()
        loaded.score = 350.0
        loaded.numKeystoneUpgrades = 3
        runRepository.saveAndFlush(loaded)
        em.clear()

        val updated = runRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.score).isEqualTo(350.0)
        assertThat(updated.numKeystoneUpgrades).isEqualTo(3)
    }

    @Test
    fun `delete removes run`() {
        val saved = runRepository.save(run())
        em.flush()

        runRepository.delete(saved)
        em.flush()

        assertThat(runRepository.findById(saved.id!!)).isEmpty
    }

    private fun run(
        season: String = "season-mn-2",
        keystoneRunId: Long = 9001,
        dungeonName: String = "Den of Nalorakk",
        completedAt: Instant? = Instant.parse("2025-03-01T12:00:00Z"),
    ) = Run(
        season = season,
        keystoneRunId = keystoneRunId,
        dungeonName = dungeonName,
        dungeonShortName = "DON",
        mythicLevel = 22,
        score = 320.5,
        timed = true,
        numKeystoneUpgrades = 2,
        clearTimeMs = 1_234_567L,
        keystoneTimeMs = 1_800_000L,
        completedAt = completedAt,
        url = "https://raider.io/runs/$keystoneRunId",
    )
}
