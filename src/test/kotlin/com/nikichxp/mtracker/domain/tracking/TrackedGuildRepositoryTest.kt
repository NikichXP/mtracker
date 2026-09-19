package com.nikichxp.mtracker.domain.tracking

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
class TrackedGuildRepositoryTest @Autowired constructor(
    private val trackedGuildRepository: TrackedGuildRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = trackedGuildRepository.save(guild())
        em.flush()
        em.clear()

        val loaded = trackedGuildRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.name).isEqualTo("Bloodline")
        assertThat(loaded.realm).isEqualTo("Gordunni")
        assertThat(loaded.addedAt).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"))
    }

    @Test
    fun `name and realm pair is unique`() {
        trackedGuildRepository.save(guild())
        em.flush()

        assertThatThrownBy {
            trackedGuildRepository.saveAndFlush(guild())
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `same name is allowed on a different realm`() {
        trackedGuildRepository.save(guild())
        val other = trackedGuildRepository.saveAndFlush(guild(realm = "Ravencrest"))

        assertThat(other.id).isNotNull()
    }

    @Test
    fun `update persists changes`() {
        val saved = trackedGuildRepository.save(guild())
        em.flush()
        em.clear()

        val loaded = trackedGuildRepository.findById(saved.id!!).orElseThrow()
        loaded.realm = "Ravencrest"
        trackedGuildRepository.saveAndFlush(loaded)
        em.clear()

        assertThat(trackedGuildRepository.findById(saved.id!!).orElseThrow().realm).isEqualTo("Ravencrest")
    }

    @Test
    fun `delete removes guild`() {
        val saved = trackedGuildRepository.save(guild())
        em.flush()

        trackedGuildRepository.delete(saved)
        em.flush()

        assertThat(trackedGuildRepository.findById(saved.id!!)).isEmpty
    }

    private fun guild(
        name: String = "Bloodline",
        realm: String = "Gordunni",
    ) = TrackedGuild(
        name = name,
        realm = realm,
        addedAt = Instant.parse("2025-03-01T12:00:00Z"),
    )
}
