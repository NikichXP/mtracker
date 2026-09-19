package com.nikichxp.mtracker.domain.player

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlayerRepositoryTest @Autowired constructor(
    private val playerRepository: PlayerRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = playerRepository.save(player())
        em.flush()
        em.clear()

        val loaded = playerRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.playerKey).isEqualTo("player-1")
        assertThat(loaded.displayName).isEqualTo("Nikita")
        assertThat(loaded.characterKeys).containsExactlyInAnyOrder("Illidan-ravencrest", "Jaina-gordunni")
        assertThat(loaded.isFriend).isTrue()
        assertThat(loaded.isGuildMember).isFalse()
        assertThat(loaded.rioScore).isEqualTo(3123.4)
        assertThat(loaded.lastSyncedAt).isEqualTo(Instant.parse("2025-03-02T12:00:00Z"))
        assertThat(loaded.nextUpdateAt).isEqualTo(Instant.parse("2025-03-03T12:00:00Z"))
    }

    @Test
    fun `playerKey is unique`() {
        playerRepository.save(player())
        em.flush()

        assertThatThrownBy {
            playerRepository.saveAndFlush(player(displayName = "Other"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `findByPlayerKey returns matching player`() {
        playerRepository.save(player())
        playerRepository.save(player(playerKey = "player-2", displayName = "Other"))

        assertThat(playerRepository.findByPlayerKey("player-1")?.displayName).isEqualTo("Nikita")
        assertThat(playerRepository.findByPlayerKey("missing")).isNull()
    }

    @Test
    fun `findByCharacterKeysIn returns distinct players owning any of the keys`() {
        playerRepository.save(player(playerKey = "p1", characterKeys = listOf("a-x", "b-y")))
        playerRepository.save(player(playerKey = "p2", characterKeys = listOf("c-z")))
        playerRepository.save(player(playerKey = "p3", characterKeys = listOf("d-w")))

        val found = playerRepository.findByCharacterKeysIn(listOf("a-x", "b-y", "c-z"))

        assertThat(found.map { it.playerKey }).containsExactlyInAnyOrder("p1", "p2")
    }

    @Test
    fun `findByCharacterKeysIn with empty collection returns empty list`() {
        playerRepository.save(player())

        assertThat(playerRepository.findByCharacterKeysIn(emptyList())).isEmpty()
    }

    @Test
    fun `findDueForUpdate returns never-synced players first then overdue`() {
        playerRepository.save(player(playerKey = "future", nextUpdateAt = Instant.parse("2025-03-10T00:00:00Z")))
        playerRepository.save(player(playerKey = "never", nextUpdateAt = null))
        playerRepository.save(player(playerKey = "overdue", nextUpdateAt = Instant.parse("2025-03-01T00:00:00Z")))

        val due = playerRepository.findDueForUpdate(Instant.parse("2025-03-05T00:00:00Z"), PageRequest.of(0, 10))

        assertThat(due.map { it.playerKey }).containsExactly("never", "overdue")
    }

    @Test
    fun `findDueForUpdate includes player whose nextUpdateAt equals now`() {
        val now = Instant.parse("2025-03-05T00:00:00Z")
        playerRepository.save(player(playerKey = "exact", nextUpdateAt = now))
        playerRepository.save(player(playerKey = "future", nextUpdateAt = now.plusSeconds(1)))

        val due = playerRepository.findDueForUpdate(now, PageRequest.of(0, 10))

        assertThat(due.map { it.playerKey }).containsExactly("exact")
    }

    @Test
    fun `findDueForUpdate orders non-null nextUpdateAt ascending after nulls`() {
        playerRepository.save(player(playerKey = "never", nextUpdateAt = null))
        playerRepository.save(player(playerKey = "later", nextUpdateAt = Instant.parse("2025-03-03T00:00:00Z")))
        playerRepository.save(player(playerKey = "sooner", nextUpdateAt = Instant.parse("2025-03-01T00:00:00Z")))

        val due = playerRepository.findDueForUpdate(Instant.parse("2025-03-05T00:00:00Z"), PageRequest.of(0, 10))

        assertThat(due.map { it.playerKey }).containsExactly("never", "sooner", "later")
    }

    @Test
    fun `findDueForUpdate honors pageable`() {
        playerRepository.save(player(playerKey = "never", nextUpdateAt = null))
        playerRepository.save(player(playerKey = "overdue", nextUpdateAt = Instant.parse("2025-03-01T00:00:00Z")))

        val due = playerRepository.findDueForUpdate(Instant.parse("2025-03-05T00:00:00Z"), PageRequest.of(0, 1))

        assertThat(due.map { it.playerKey }).containsExactly("never")
    }

    @Test
    fun `findByRioScoreGreaterThan returns matching players`() {
        playerRepository.save(player(playerKey = "low", rioScore = 100.0))
        playerRepository.save(player(playerKey = "high", rioScore = 3000.0))
        playerRepository.save(player(playerKey = "none", rioScore = null))

        val found = playerRepository.findByRioScoreGreaterThan(500.0)

        assertThat(found.map { it.playerKey }).containsExactly("high")
    }

    @Test
    fun `findByRioScoreGreaterThan excludes exact match`() {
        playerRepository.save(player(playerKey = "exact", rioScore = 500.0))
        playerRepository.save(player(playerKey = "above", rioScore = 500.1))

        val found = playerRepository.findByRioScoreGreaterThan(500.0)

        assertThat(found.map { it.playerKey }).containsExactly("above")
    }

    @Test
    fun `adding element to characterKeys persists`() {
        val saved = playerRepository.save(player(characterKeys = listOf("a-x")))
        em.flush()
        em.clear()

        val loaded = playerRepository.findById(saved.id!!).orElseThrow()
        (loaded.characterKeys as MutableList<String>).add("b-y")
        em.flush()
        em.clear()

        assertThat(playerRepository.findById(saved.id!!).orElseThrow().characterKeys)
            .containsExactlyInAnyOrder("a-x", "b-y")
    }

    @Test
    fun `update persists changes`() {
        val saved = playerRepository.save(player())
        em.flush()
        em.clear()

        val loaded = playerRepository.findById(saved.id!!).orElseThrow()
        loaded.displayName = "Renamed"
        loaded.rioScore = 3500.0
        playerRepository.saveAndFlush(loaded)
        em.clear()

        val updated = playerRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.displayName).isEqualTo("Renamed")
        assertThat(updated.rioScore).isEqualTo(3500.0)
    }

    @Test
    fun `delete removes player`() {
        val saved = playerRepository.save(player())
        em.flush()

        playerRepository.delete(saved)
        em.flush()

        assertThat(playerRepository.findById(saved.id!!)).isEmpty
    }

    private fun player(
        playerKey: String = "player-1",
        displayName: String = "Nikita",
        characterKeys: List<String> = listOf("Illidan-ravencrest", "Jaina-gordunni"),
        rioScore: Double? = 3123.4,
        nextUpdateAt: Instant? = Instant.parse("2025-03-03T12:00:00Z"),
    ) = Player(
        playerKey = playerKey,
        displayName = displayName,
        characterKeys = characterKeys,
        isFriend = true,
        isGuildMember = false,
        rioScore = rioScore,
        lastSyncedAt = Instant.parse("2025-03-02T12:00:00Z"),
        nextUpdateAt = nextUpdateAt,
    )
}
