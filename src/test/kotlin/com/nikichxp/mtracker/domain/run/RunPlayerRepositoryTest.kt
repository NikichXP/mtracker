package com.nikichxp.mtracker.domain.run

import com.nikichxp.mtracker.domain.player.Player
import com.nikichxp.mtracker.domain.player.PlayerRepository
import jakarta.persistence.PersistenceException
import org.hibernate.TransientPropertyValueException
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
class RunPlayerRepositoryTest @Autowired constructor(
    private val runPlayerRepository: RunPlayerRepository,
    private val runRepository: RunRepository,
    private val playerRepository: PlayerRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields including run and player references`() {
        val run = runRepository.save(run())
        val player = playerRepository.save(Player(playerKey = "p1", displayName = "Nikita"))
        val saved = runPlayerRepository.save(runPlayer(run, player))
        em.flush()
        em.clear()

        val loaded = runPlayerRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.run.id).isEqualTo(run.id)
        assertThat(loaded.player?.id).isEqualTo(player.id)
        assertThat(loaded.characterKey).isEqualTo("Illidan-ravencrest")
        assertThat(loaded.name).isEqualTo("Illidan")
        assertThat(loaded.realm).isEqualTo("Ravencrest")
        assertThat(loaded.region).isEqualTo("eu")
        assertThat(loaded.characterClass).isEqualTo("Demon Hunter")
        assertThat(loaded.spec).isEqualTo("Havoc")
        assertThat(loaded.role).isEqualTo("dps")
        assertThat(loaded.guildName).isEqualTo("Bloodline")
        assertThat(loaded.itemLevel).isEqualTo(250.5)
        assertThat(loaded.rioScore).isEqualTo(3123.4)
    }

    @Test
    fun `player reference is nullable`() {
        val run = runRepository.save(run())
        val saved = runPlayerRepository.save(runPlayer(run, player = null))
        em.flush()
        em.clear()

        assertThat(runPlayerRepository.findById(saved.id!!).orElseThrow().player).isNull()
    }

    @Test
    fun `run reference does not cascade - transient run fails`() {
        val transientRun = run()

        assertThatThrownBy {
            runPlayerRepository.saveAndFlush(runPlayer(transientRun))
        }.rootCause().isInstanceOf(TransientPropertyValueException::class.java)
    }

    @Test
    fun `deleting a run with run players violates foreign key`() {
        val run = runRepository.save(run())
        runPlayerRepository.save(runPlayer(run))
        em.flush()

        // Native delete bypasses Hibernate's cascade validation and hits the DB FK constraint
        assertThatThrownBy {
            em.entityManager.createNativeQuery("delete from runs where id = :id")
                .setParameter("id", run.id)
                .executeUpdate()
        }.isInstanceOf(PersistenceException::class.java)
    }

    @Test
    fun `nullable fields persist as null`() {
        val run = runRepository.save(run())
        val saved = runPlayerRepository.save(
            RunPlayer(run = run, characterKey = "Min-realm", name = "Min", realm = "Realm")
        )
        em.flush()
        em.clear()

        val loaded = runPlayerRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.player).isNull()
        assertThat(loaded.region).isNull()
        assertThat(loaded.characterClass).isNull()
        assertThat(loaded.spec).isNull()
        assertThat(loaded.role).isNull()
        assertThat(loaded.guildName).isNull()
        assertThat(loaded.itemLevel).isNull()
        assertThat(loaded.rioScore).isNull()
    }

    @Test
    fun `run and characterKey pair is unique`() {
        val run = runRepository.save(run())
        runPlayerRepository.save(runPlayer(run))
        em.flush()

        assertThatThrownBy {
            runPlayerRepository.saveAndFlush(runPlayer(run, name = "Other"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `same characterKey is allowed in a different run`() {
        val run1 = runRepository.save(run(keystoneRunId = 1))
        val run2 = runRepository.save(run(keystoneRunId = 2))
        runPlayerRepository.save(runPlayer(run1))
        val second = runPlayerRepository.saveAndFlush(runPlayer(run2))

        assertThat(second.id).isNotNull()
    }

    @Test
    fun `findRecentByCharacterKeyIn orders by run completedAt desc and honors pageable`() {
        val oldRun = runRepository.save(run(keystoneRunId = 1, completedAt = Instant.parse("2025-03-01T00:00:00Z")))
        val newRun = runRepository.save(run(keystoneRunId = 2, completedAt = Instant.parse("2025-03-05T00:00:00Z")))
        val midRun = runRepository.save(run(keystoneRunId = 3, completedAt = Instant.parse("2025-03-03T00:00:00Z")))
        runPlayerRepository.save(runPlayer(oldRun))
        runPlayerRepository.save(runPlayer(newRun))
        runPlayerRepository.save(runPlayer(midRun))
        runPlayerRepository.save(runPlayer(oldRun, characterKey = "Other-realm"))
        em.flush()
        em.clear()

        val recent = runPlayerRepository.findRecentByCharacterKeyIn(
            listOf("Illidan-ravencrest"), PageRequest.of(0, 10)
        )
        assertThat(recent.map { it.run.keystoneRunId }).containsExactly(2L, 3L, 1L)

        val topTwo = runPlayerRepository.findRecentByCharacterKeyIn(
            listOf("Illidan-ravencrest"), PageRequest.of(0, 2)
        )
        assertThat(topTwo.map { it.run.keystoneRunId }).containsExactly(2L, 3L)

        val pageTwo = runPlayerRepository.findRecentByCharacterKeyIn(
            listOf("Illidan-ravencrest"), PageRequest.of(1, 2)
        )
        assertThat(pageTwo.map { it.run.keystoneRunId }).containsExactly(1L)
    }

    @Test
    fun `findRecentByCharacterKeyIn matches multiple keys`() {
        val run1 = runRepository.save(run(keystoneRunId = 1, completedAt = Instant.parse("2025-03-01T00:00:00Z")))
        val run2 = runRepository.save(run(keystoneRunId = 2, completedAt = Instant.parse("2025-03-05T00:00:00Z")))
        runPlayerRepository.save(runPlayer(run1, characterKey = "a-x"))
        runPlayerRepository.save(runPlayer(run2, characterKey = "b-y"))
        runPlayerRepository.save(runPlayer(run2, characterKey = "c-z"))
        em.flush()
        em.clear()

        val found = runPlayerRepository.findRecentByCharacterKeyIn(
            listOf("a-x", "b-y"), PageRequest.of(0, 10)
        )

        assertThat(found.map { it.characterKey }).containsExactly("b-y", "a-x")
    }

    @Test
    fun `findByRunIdIn returns roster of given runs`() {
        val run1 = runRepository.save(run(keystoneRunId = 1))
        val run2 = runRepository.save(run(keystoneRunId = 2))
        runPlayerRepository.save(runPlayer(run1, characterKey = "a-x"))
        runPlayerRepository.save(runPlayer(run1, characterKey = "b-y"))
        runPlayerRepository.save(runPlayer(run2, characterKey = "c-z"))
        em.flush()
        em.clear()

        val found = runPlayerRepository.findByRunIdIn(listOf(run1.id!!))

        assertThat(found.map { it.characterKey }).containsExactlyInAnyOrder("a-x", "b-y")
    }

    @Test
    fun `findByPlayerIdIn returns run players linked to given players`() {
        val run = runRepository.save(run())
        val p1 = playerRepository.save(Player(playerKey = "p1"))
        val p2 = playerRepository.save(Player(playerKey = "p2"))
        runPlayerRepository.save(runPlayer(run, p1, characterKey = "a-x"))
        runPlayerRepository.save(runPlayer(run, p2, characterKey = "b-y"))
        runPlayerRepository.save(runPlayer(run, player = null, characterKey = "c-z"))
        em.flush()
        em.clear()

        val found = runPlayerRepository.findByPlayerIdIn(listOf(p1.id!!))

        assertThat(found.map { it.characterKey }).containsExactly("a-x")
    }

    @Test
    fun `findByCharacterKey returns matching run players`() {
        val run = runRepository.save(run())
        runPlayerRepository.save(runPlayer(run))
        runPlayerRepository.save(runPlayer(run, characterKey = "Other-realm"))

        val found = runPlayerRepository.findByCharacterKey("Illidan-ravencrest")

        assertThat(found.map { it.characterKey }).containsExactly("Illidan-ravencrest")
    }

    @Test
    fun `update persists changes`() {
        val run = runRepository.save(run())
        val saved = runPlayerRepository.save(runPlayer(run))
        em.flush()
        em.clear()

        val loaded = runPlayerRepository.findById(saved.id!!).orElseThrow()
        loaded.rioScore = 3500.0
        loaded.guildName = "New Guild"
        runPlayerRepository.saveAndFlush(loaded)
        em.clear()

        val updated = runPlayerRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.rioScore).isEqualTo(3500.0)
        assertThat(updated.guildName).isEqualTo("New Guild")
    }

    @Test
    fun `delete removes run player`() {
        val run = runRepository.save(run())
        val saved = runPlayerRepository.save(runPlayer(run))
        em.flush()

        runPlayerRepository.delete(saved)
        em.flush()

        assertThat(runPlayerRepository.findById(saved.id!!)).isEmpty
    }

    private fun run(
        keystoneRunId: Long = 9001,
        completedAt: Instant? = Instant.parse("2025-03-01T12:00:00Z"),
    ) = Run(
        season = "season-mn-2",
        keystoneRunId = keystoneRunId,
        dungeonName = "Den of Nalorakk",
        mythicLevel = 22,
        completedAt = completedAt,
    )

    private fun runPlayer(
        run: Run,
        player: Player? = null,
        characterKey: String = "Illidan-ravencrest",
        name: String = "Illidan",
    ) = RunPlayer(
        run = run,
        characterKey = characterKey,
        name = name,
        realm = "Ravencrest",
        region = "eu",
        characterClass = "Demon Hunter",
        spec = "Havoc",
        role = "dps",
        guildName = "Bloodline",
        itemLevel = 250.5,
        rioScore = 3123.4,
        player = player,
    )
}
