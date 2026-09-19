package com.nikichxp.mtracker.domain.tracking

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrackedPlayerRepositoryTest @Autowired constructor(
    private val trackedPlayerRepository: TrackedPlayerRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = trackedPlayerRepository.save(trackedPlayer())
        em.flush()
        em.clear()

        val loaded = trackedPlayerRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.displayName).isEqualTo("Nikita")
        assertThat(loaded.characterKeys).containsExactlyInAnyOrder("Illidan-ravencrest", "Jaina-gordunni")
        assertThat(loaded.isFriend).isTrue()
        assertThat(loaded.addedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
    }

    @Test
    fun `characterKeys element collection survives round-trip`() {
        val saved = trackedPlayerRepository.save(
            trackedPlayer(characterKeys = listOf("a-x", "b-y", "c-z"))
        )
        em.flush()
        em.clear()

        assertThat(trackedPlayerRepository.findById(saved.id!!).orElseThrow().characterKeys)
            .containsExactlyInAnyOrder("a-x", "b-y", "c-z")
    }

    @Test
    fun `update persists changes`() {
        val saved = trackedPlayerRepository.save(trackedPlayer())
        em.flush()
        em.clear()

        val loaded = trackedPlayerRepository.findById(saved.id!!).orElseThrow()
        loaded.displayName = "Renamed"
        loaded.characterKeys = listOf("only-key")
        em.flush()
        em.clear()

        val updated = trackedPlayerRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.displayName).isEqualTo("Renamed")
        assertThat(updated.characterKeys).containsExactly("only-key")
    }

    @Test
    fun `delete removes tracked player`() {
        val saved = trackedPlayerRepository.save(trackedPlayer())
        em.flush()

        trackedPlayerRepository.delete(saved)
        em.flush()

        assertThat(trackedPlayerRepository.findById(saved.id!!)).isEmpty
    }

    private fun trackedPlayer(
        displayName: String = "Nikita",
        characterKeys: List<String> = listOf("Illidan-ravencrest", "Jaina-gordunni"),
    ) = TrackedPlayer(
        displayName = displayName,
        characterKeys = characterKeys,
        isFriend = true,
        addedAt = Instant.parse("2025-03-01T12:00:00Z"),
    )
}
