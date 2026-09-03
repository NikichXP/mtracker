package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.domain.Character
import com.nikichxp.mtracker.domain.CharacterRepository
import com.nikichxp.mtracker.domain.CharacterSource
import com.nikichxp.mtracker.domain.DungeonRun
import com.nikichxp.mtracker.domain.WeekKeyCalculator
import com.nikichxp.mtracker.domain.WeeklySnapshot
import com.nikichxp.mtracker.domain.WeeklySnapshotRepository
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.CharacterProfileDto
import com.nikichxp.mtracker.raiderio.dto.KeystoneRunDto
import com.nikichxp.mtracker.config.MtrackerProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
@Transactional
class CharacterSyncService(
    private val raiderIoService: IRaiderIoService,
    private val characterRepository: CharacterRepository,
    private val weeklySnapshotRepository: WeeklySnapshotRepository,
    private val props: MtrackerProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun syncCharacter(characterKey: String, source: CharacterSource) {
        val (name, realm) = splitKey(characterKey) ?: run {
            log.warn("Skipping malformed character key '{}'", characterKey)
            return
        }

        val profile = raiderIoService.fetchCharacterProfile(name, realm)
        if (profile == null) {
            log.warn("No Raider.io profile for {} ({}), skipping sync", characterKey, source)
            return
        }

        val existing = characterRepository.findByCharacterKey(characterKey)
        val character = toCharacter(characterKey, profile, source, existing)
        characterRepository.save(character)

        upsertWeeklySnapshot(characterKey, character)
    }

    private fun splitKey(characterKey: String): Pair<String, String>? {
        val idx = characterKey.lastIndexOf('-')
        if (idx <= 0 || idx == characterKey.length - 1) return null
        return characterKey.substring(0, idx) to characterKey.substring(idx + 1)
    }

    private fun toCharacter(
        characterKey: String,
        profile: CharacterProfileDto,
        source: CharacterSource,
        existing: Character?,
    ): Character {
        val score = profile.mythicPlusScoresBySeason.firstOrNull()?.scores?.all
        return Character(
            id = existing?.id,
            characterKey = characterKey,
            name = profile.name ?: existing?.name ?: characterKey,
            realm = profile.realm ?: existing?.realm ?: "",
            region = props.region,
            characterClass = profile.characterClass,
            race = profile.race,
            faction = profile.faction,
            activeSpecName = profile.activeSpecName,
            activeSpecRole = profile.activeSpecRole,
            guildName = profile.guild?.name,
            itemLevelEquipped = profile.gear?.itemLevelEquipped,
            mythicPlusScore = score,
            weeklyRuns = profile.mythicPlusWeeklyHighestLevelRuns.mapNotNull(::toDungeonRun),
            recentRuns = profile.mythicPlusRecentRuns.mapNotNull(::toDungeonRun),
            bestRuns = profile.mythicPlusBestRuns.mapNotNull(::toDungeonRun),
            source = source,
            lastSyncedAt = Instant.now(),
        )
    }

    private fun toDungeonRun(dto: KeystoneRunDto): DungeonRun? {
        val dungeonName = dto.dungeon ?: return null
        val level = dto.mythicLevel ?: return null
        return DungeonRun(
            dungeonName = dungeonName,
            mythicLevel = level,
            score = dto.score ?: 0.0,
            timed = dto.clearedInTime ?: false,
            numKeystoneUpgrades = dto.numKeystoneUpgrades ?: 0,
            completedAt = dto.completedAt,
            url = dto.url,
        )
    }

    private fun upsertWeeklySnapshot(characterKey: String, character: Character) {
        val weekKey = WeekKeyCalculator.currentWeekKey()
        val existing = weeklySnapshotRepository.findByWeekKeyAndCharacterKey(weekKey, characterKey)
        val snapshot = WeeklySnapshot(
            id = existing?.id,
            weekKey = weekKey,
            characterKey = characterKey,
            mythicPlusScore = character.mythicPlusScore,
            weeklyRunsCount = character.weeklyRuns.size,
            weeklyHighestLevel = character.weeklyRuns.maxOfOrNull { it.mythicLevel } ?: 0,
            capturedAt = Instant.now(),
        )
        weeklySnapshotRepository.save(snapshot)
    }
}
