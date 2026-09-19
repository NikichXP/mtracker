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
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TopPlayerScanTaskRepositoryTest @Autowired constructor(
    private val taskRepository: TopPlayerScanTaskRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = taskRepository.save(task())
        em.flush()
        em.clear()

        val loaded = taskRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.region).isEqualTo("eu")
        assertThat(loaded.season).isEqualTo("season-mn-2")
        assertThat(loaded.characterKey).isEqualTo("Illidan-ravencrest")
        assertThat(loaded.name).isEqualTo("Illidan")
        assertThat(loaded.realmSlug).isEqualTo("ravencrest")
        assertThat(loaded.className).isEqualTo("Demon Hunter")
        assertThat(loaded.specId).isEqualTo(577)
        assertThat(loaded.specName).isEqualTo("Havoc")
        assertThat(loaded.specSlug).isEqualTo("havoc")
        assertThat(loaded.role).isEqualTo("dps")
        assertThat(loaded.rioScore).isEqualTo(3123.4)
        assertThat(loaded.rank).isEqualTo(5)
        assertThat(loaded.talentImportString).isEqualTo("talent-string")
        assertThat(loaded.guildName).isEqualTo("Bloodline")
        assertThat(loaded.createdAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
        assertThat(loaded.claimedAt).isEqualTo(Instant.parse("2025-03-02T12:00:00Z"))
        assertThat(loaded.claimedBy).isEqualTo("worker-1")
        assertThat(loaded.attempts).isEqualTo(2)
        assertThat(loaded.version).isEqualTo(0L)
    }

    @Test
    fun `season region characterKey and specId tuple is unique`() {
        taskRepository.save(task())
        em.flush()

        assertThatThrownBy {
            taskRepository.saveAndFlush(task(name = "Other"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `same characterKey is allowed with a different specId`() {
        taskRepository.save(task())
        val other = taskRepository.saveAndFlush(task(specId = 581))

        assertThat(other.id).isNotNull()
    }

    @Test
    fun `version increments on update`() {
        val saved = taskRepository.save(task())
        em.flush()
        em.clear()

        val loaded = taskRepository.findById(saved.id!!).orElseThrow()
        assertThat(loaded.version).isEqualTo(0L)

        loaded.attempts = 3
        taskRepository.saveAndFlush(loaded)
        em.clear()

        assertThat(taskRepository.findById(saved.id!!).orElseThrow().version).isEqualTo(1L)
    }

    @Test
    fun `stale update fails with optimistic locking`() {
        val saved = taskRepository.saveAndFlush(task())
        em.clear()

        val stale = taskRepository.findById(saved.id!!).orElseThrow()
        em.clear()

        val fresh = taskRepository.findById(saved.id!!).orElseThrow()
        fresh.attempts = 1
        taskRepository.saveAndFlush(fresh)

        stale.attempts = 2
        assertThatThrownBy {
            taskRepository.saveAndFlush(stale)
        }.isInstanceOf(OptimisticLockingFailureException::class.java)
    }

    @Test
    fun `findClaimable returns unclaimed and stale-claimed tasks ordered by rioScore desc`() {
        val now = Instant.parse("2025-03-05T12:00:00Z")
        val staleBefore = now.minusSeconds(3600)
        taskRepository.save(task(characterKey = "unclaimed", rioScore = 100.0, claimedAt = null))
        taskRepository.save(
            task(characterKey = "stale", specId = 581, rioScore = 300.0,
                claimedAt = now.minusSeconds(7200))
        )
        taskRepository.save(
            task(characterKey = "fresh", specId = 102, rioScore = 200.0,
                claimedAt = now.minusSeconds(60))
        )
        em.flush()
        em.clear()

        val claimable = taskRepository.findClaimable(staleBefore, PageRequest.of(0, 10))

        assertThat(claimable.map { it.characterKey }).containsExactly("stale", "unclaimed")
    }

    @Test
    fun `findClaimable excludes task claimed exactly at staleBefore`() {
        val staleBefore = Instant.parse("2025-03-05T12:00:00Z")
        taskRepository.save(task(characterKey = "boundary", claimedAt = staleBefore))
        taskRepository.save(task(characterKey = "stale", specId = 581, claimedAt = staleBefore.minusSeconds(1)))
        em.flush()
        em.clear()

        val claimable = taskRepository.findClaimable(staleBefore, PageRequest.of(0, 10))

        assertThat(claimable.map { it.characterKey }).containsExactly("stale")
    }

    @Test
    fun `findClaimable honors pageable`() {
        taskRepository.save(task(characterKey = "a-x", rioScore = 100.0, claimedAt = null))
        taskRepository.save(task(characterKey = "b-y", specId = 581, rioScore = 300.0, claimedAt = null))
        taskRepository.save(task(characterKey = "c-z", specId = 102, rioScore = 200.0, claimedAt = null))
        em.flush()
        em.clear()

        val claimable = taskRepository.findClaimable(Instant.parse("2025-03-05T12:00:00Z"), PageRequest.of(0, 2))

        assertThat(claimable.map { it.characterKey }).containsExactly("b-y", "c-z")
    }

    @Test
    fun `deleteByCreatedAtBefore deletes old tasks and returns count`() {
        taskRepository.save(task(characterKey = "old-1", createdAt = Instant.parse("2025-01-01T00:00:00Z")))
        taskRepository.save(
            task(characterKey = "old-2", specId = 581, createdAt = Instant.parse("2025-01-02T00:00:00Z"))
        )
        taskRepository.save(task(characterKey = "new", specId = 102, createdAt = Instant.parse("2025-03-01T00:00:00Z")))
        em.flush()
        em.clear()

        val deleted = taskRepository.deleteByCreatedAtBefore(Instant.parse("2025-02-01T00:00:00Z"))

        assertThat(deleted).isEqualTo(2L)
        assertThat(taskRepository.findAll().map { it.characterKey }).containsExactly("new")
    }

    @Test
    fun `deleteByCreatedAtBefore keeps task created exactly at cutoff`() {
        val cutoff = Instant.parse("2025-02-01T00:00:00Z")
        taskRepository.save(task(characterKey = "boundary", createdAt = cutoff))
        em.flush()
        em.clear()

        assertThat(taskRepository.deleteByCreatedAtBefore(cutoff)).isEqualTo(0L)
        assertThat(taskRepository.findAll().map { it.characterKey }).containsExactly("boundary")
    }

    @Test
    fun `nullable fields persist as null`() {
        val saved = taskRepository.save(
            task().apply {
                talentImportString = null
                guildName = null
                claimedAt = null
                claimedBy = null
            }
        )
        em.flush()
        em.clear()

        val loaded = taskRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.talentImportString).isNull()
        assertThat(loaded.guildName).isNull()
        assertThat(loaded.claimedAt).isNull()
        assertThat(loaded.claimedBy).isNull()
    }

    @Test
    fun `long talentImportString round-trips`() {
        val longString = "x".repeat(2048)
        val saved = taskRepository.save(task().apply { talentImportString = longString })
        em.flush()
        em.clear()

        assertThat(taskRepository.findById(saved.id!!).orElseThrow().talentImportString)
            .isEqualTo(longString)
    }

    @Test
    fun `countBySeasonAndRegion counts matching tasks`() {
        taskRepository.save(task())
        taskRepository.save(task(characterKey = "b-y", specId = 581))
        taskRepository.save(task(season = "season-tww-1", characterKey = "c-z", specId = 102))

        assertThat(taskRepository.countBySeasonAndRegion("season-mn-2", "eu")).isEqualTo(2L)
        assertThat(taskRepository.countBySeasonAndRegion("season-tww-1", "eu")).isEqualTo(1L)
        assertThat(taskRepository.countBySeasonAndRegion("season-mn-2", "us")).isEqualTo(0L)
    }

    @Test
    fun `exists and find by identity`() {
        taskRepository.save(task())

        assertThat(
            taskRepository.existsBySeasonAndRegionAndCharacterKeyAndSpecId(
                "season-mn-2", "eu", "Illidan-ravencrest", 577
            )
        ).isTrue()
        assertThat(
            taskRepository.existsBySeasonAndRegionAndCharacterKeyAndSpecId(
                "season-mn-2", "eu", "Illidan-ravencrest", 581
            )
        ).isFalse()

        val found = taskRepository.findBySeasonAndRegionAndCharacterKeyAndSpecId(
            "season-mn-2", "eu", "Illidan-ravencrest", 577
        )
        assertThat(found).isNotNull()
        assertThat(found!!.name).isEqualTo("Illidan")
    }

    @Test
    fun `update persists changes`() {
        val saved = taskRepository.save(task())
        em.flush()
        em.clear()

        val loaded = taskRepository.findById(saved.id!!).orElseThrow()
        loaded.claimedBy = "worker-2"
        loaded.attempts = 7
        taskRepository.saveAndFlush(loaded)
        em.clear()

        val updated = taskRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.claimedBy).isEqualTo("worker-2")
        assertThat(updated.attempts).isEqualTo(7)
    }

    @Test
    fun `delete removes task`() {
        val saved = taskRepository.save(task())
        em.flush()

        taskRepository.delete(saved)
        em.flush()

        assertThat(taskRepository.findById(saved.id!!)).isEmpty
    }

    private fun task(
        characterKey: String = "Illidan-ravencrest",
        name: String = "Illidan",
        season: String = "season-mn-2",
        region: String = "eu",
        specId: Int = 577,
        rioScore: Double = 3123.4,
        createdAt: Instant = Instant.parse("2025-03-01T12:00:00Z"),
        claimedAt: Instant? = Instant.parse("2025-03-02T12:00:00Z"),
    ) = TopPlayerScanTask(
        region = region,
        season = season,
        characterKey = characterKey,
        name = name,
        realmSlug = "ravencrest",
        className = "Demon Hunter",
        specId = specId,
        specName = "Havoc",
        specSlug = "havoc",
        role = "dps",
        rioScore = rioScore,
        rank = 5,
        talentImportString = "talent-string",
        guildName = "Bloodline",
        createdAt = createdAt,
        claimedAt = claimedAt,
        claimedBy = if (claimedAt != null) "worker-1" else null,
        attempts = 2,
    )
}
