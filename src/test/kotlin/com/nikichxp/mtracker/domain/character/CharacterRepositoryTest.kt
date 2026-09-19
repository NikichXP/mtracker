package com.nikichxp.mtracker.domain.character

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
class CharacterRepositoryTest @Autowired constructor(
    private val characterRepository: CharacterRepository,
    private val em: TestEntityManager,
) {

    @Test
    fun `save and read back preserves all fields`() {
        val saved = characterRepository.save(character())
        em.flush()
        em.clear()

        val loaded = characterRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.characterKey).isEqualTo("Illidan-ravencrest")
        assertThat(loaded.name).isEqualTo("Illidan")
        assertThat(loaded.realm).isEqualTo("Ravencrest")
        assertThat(loaded.region).isEqualTo("eu")
        assertThat(loaded.characterClass).isEqualTo("Demon Hunter")
        assertThat(loaded.race).isEqualTo("Night Elf")
        assertThat(loaded.faction).isEqualTo("ALLIANCE")
        assertThat(loaded.activeSpecName).isEqualTo("Havoc")
        assertThat(loaded.activeSpecRole).isEqualTo("DPS")
        assertThat(loaded.guildName).isEqualTo("Bloodline")
        assertThat(loaded.itemLevelEquipped).isEqualTo(250.5)
        assertThat(loaded.mythicPlusScore).isEqualTo(3123.4)
        assertThat(loaded.weeklyRuns).containsExactlyInAnyOrder(
            DungeonRun(
                dungeonName = "Den of Nalorakk",
                mythicLevel = 22,
                score = 320.5,
                timed = true,
                numKeystoneUpgrades = 2,
                completedAt = Instant.parse("2025-03-01T12:00:00Z"),
                url = "https://raider.io/runs/1",
            ),
            DungeonRun(dungeonName = "Skyreach", mythicLevel = 20),
        )
        assertThat(loaded.source).isEqualTo(CharacterSource.FRIEND)
        assertThat(loaded.lastSyncedAt).isEqualTo(Instant.parse("2025-03-02T12:00:00Z"))
    }

    @Test
    fun `source enum is stored as string`() {
        val saved = characterRepository.save(character(source = CharacterSource.ALT))
        em.flush()

        val raw = em.entityManager
            .createNativeQuery("select source from characters where id = :id")
            .setParameter("id", saved.id)
            .singleResult

        assertThat(raw).isEqualTo("ALT")
    }

    @Test
    fun `characterKey is unique`() {
        characterRepository.save(character())
        em.flush()

        assertThatThrownBy {
            characterRepository.saveAndFlush(character(name = "Other"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `findByCharacterKey returns matching character`() {
        characterRepository.save(character())
        characterRepository.save(character(characterKey = "Jaina-gordunni", name = "Jaina"))

        assertThat(characterRepository.findByCharacterKey("Illidan-ravencrest")?.name).isEqualTo("Illidan")
        assertThat(characterRepository.findByCharacterKey("Nobody-nowhere")).isNull()
    }

    @Test
    fun `findByCharacterKeyIn returns matching characters`() {
        characterRepository.save(character())
        characterRepository.save(character(characterKey = "Jaina-gordunni"))
        characterRepository.save(character(characterKey = "Arthas-gordunni"))

        val found = characterRepository.findByCharacterKeyIn(listOf("Illidan-ravencrest", "Arthas-gordunni"))

        assertThat(found.map { it.characterKey })
            .containsExactlyInAnyOrder("Illidan-ravencrest", "Arthas-gordunni")
    }

    @Test
    fun `nullable fields persist as null`() {
        val saved = characterRepository.save(
            Character(characterKey = "Minimal-realm", name = "Minimal", realm = "Realm", region = "eu")
        )
        em.flush()
        em.clear()

        val loaded = characterRepository.findById(saved.id!!).orElseThrow()

        assertThat(loaded.characterClass).isNull()
        assertThat(loaded.race).isNull()
        assertThat(loaded.faction).isNull()
        assertThat(loaded.activeSpecName).isNull()
        assertThat(loaded.activeSpecRole).isNull()
        assertThat(loaded.guildName).isNull()
        assertThat(loaded.itemLevelEquipped).isNull()
        assertThat(loaded.mythicPlusScore).isNull()
        assertThat(loaded.lastSyncedAt).isNull()
        assertThat(loaded.weeklyRuns).isEmpty()
    }

    @Test
    fun `update persists changes`() {
        val saved = characterRepository.save(character())
        em.flush()
        em.clear()

        val loaded = characterRepository.findById(saved.id!!).orElseThrow()
        loaded.mythicPlusScore = 4000.0
        loaded.guildName = "New Guild"
        characterRepository.saveAndFlush(loaded)
        em.clear()

        val updated = characterRepository.findById(saved.id!!).orElseThrow()
        assertThat(updated.mythicPlusScore).isEqualTo(4000.0)
        assertThat(updated.guildName).isEqualTo("New Guild")
    }

    @Test
    fun `delete removes character`() {
        val saved = characterRepository.save(character())
        em.flush()

        characterRepository.delete(saved)
        em.flush()

        assertThat(characterRepository.findById(saved.id!!)).isEmpty
    }

    private fun character(
        characterKey: String = "Illidan-ravencrest",
        name: String = "Illidan",
        source: CharacterSource = CharacterSource.FRIEND,
    ) = Character(
        characterKey = characterKey,
        name = name,
        realm = "Ravencrest",
        region = "eu",
        characterClass = "Demon Hunter",
        race = "Night Elf",
        faction = "ALLIANCE",
        activeSpecName = "Havoc",
        activeSpecRole = "DPS",
        guildName = "Bloodline",
        itemLevelEquipped = 250.5,
        mythicPlusScore = 3123.4,
        weeklyRuns = listOf(
            DungeonRun(
                dungeonName = "Den of Nalorakk",
                mythicLevel = 22,
                score = 320.5,
                timed = true,
                numKeystoneUpgrades = 2,
                completedAt = Instant.parse("2025-03-01T12:00:00Z"),
                url = "https://raider.io/runs/1",
            ),
            DungeonRun(dungeonName = "Skyreach", mythicLevel = 20),
        ),
        source = source,
        lastSyncedAt = Instant.parse("2025-03-02T12:00:00Z"),
    )
}
