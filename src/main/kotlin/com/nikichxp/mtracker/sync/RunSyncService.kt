package com.nikichxp.mtracker.sync

import com.nikichxp.mtracker.domain.Player
import com.nikichxp.mtracker.domain.Run
import com.nikichxp.mtracker.domain.RunPlayer
import com.nikichxp.mtracker.domain.RunPlayerRepository
import com.nikichxp.mtracker.domain.RunRepository
import com.nikichxp.mtracker.domain.PlayerRepository
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.KeystoneRunDto
import com.nikichxp.mtracker.raiderio.dto.RunDetailsDto
import com.nikichxp.mtracker.raiderio.dto.RunRosterMemberDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Persists recent Mythic+ runs: one [Run] row per keystone run plus 5 [RunPlayer] rows
 * (one per roster member, fetched from the Raider.io run-details endpoint).
 * Runs already present in the database are skipped, so the same run seen on several
 * tracked characters is stored (and its roster fetched) only once.
 */
@Component
class RunSyncService(
    private val raiderIoService: IRaiderIoService,
    private val runRepository: RunRepository,
    private val runPlayerRepository: RunPlayerRepository,
    private val playerRepository: PlayerRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun syncRecentRuns(recentRuns: List<KeystoneRunDto>) {
        for (runDto in recentRuns) {
            val ref = RunRef.parse(runDto.url) ?: continue
            if (runRepository.existsBySeasonAndKeystoneRunId(ref.season, ref.keystoneRunId)) continue
            val details = raiderIoService.fetchRunDetails(ref.season, ref.keystoneRunId) ?: continue
            storeRun(ref, runDto.url, details)
        }
    }

    private fun storeRun(ref: RunRef, url: String?, details: RunDetailsDto) {
        val chests = details.numChests ?: 0
        val run = runRepository.save(
            Run(
                season = ref.season,
                keystoneRunId = ref.keystoneRunId,
                dungeonName = details.dungeon?.name ?: "",
                dungeonShortName = details.dungeon?.shortName,
                mythicLevel = details.mythicLevel ?: 0,
                score = details.score,
                timed = chests > 0,
                numKeystoneUpgrades = chests,
                clearTimeMs = details.clearTimeMs,
                keystoneTimeMs = details.keystoneTimeMs,
                completedAt = details.completedAt,
                url = url,
            )
        )

        val playersByKey = playersByCharacterKey(details.roster)
        val rosterRows = details.roster.mapNotNull { toRunPlayer(run, it, playersByKey) }
        runPlayerRepository.saveAll(rosterRows)
        log.debug(
            "Stored run {}/{} ({} +{}) with {} roster members",
            ref.season, ref.keystoneRunId, run.dungeonName, run.mythicLevel, rosterRows.size,
        )
    }

    private fun playersByCharacterKey(roster: List<RunRosterMemberDto>): Map<String, Player> {
        val keys = roster.mapNotNull { member ->
            val name = member.character?.name ?: return@mapNotNull null
            val realm = member.character.realm?.name ?: return@mapNotNull null
            "$name-$realm"
        }
        if (keys.isEmpty()) return emptyMap()
        return playerRepository.findByCharacterKeysIn(keys)
            .flatMap { player -> player.characterKeys.map { key -> key to player } }
            .toMap()
    }

    private fun toRunPlayer(run: Run, member: RunRosterMemberDto, playersByKey: Map<String, Player>): RunPlayer? {
        val character = member.character ?: return null
        val name = character.name ?: return null
        val realm = character.realm?.name ?: return null
        val characterKey = "$name-$realm"
        return RunPlayer(
            run = run,
            characterKey = characterKey,
            name = name,
            realm = realm,
            region = character.region?.slug,
            characterClass = character.characterClass?.name,
            spec = character.spec?.name,
            role = member.role ?: character.spec?.role,
            guildName = member.guild?.name,
            itemLevel = member.items?.itemLevelEquipped,
            rioScore = member.ranks?.score,
            player = playersByKey[characterKey],
        )
    }
}

/** Reference to a Raider.io keystone run, parsed from the run URL (`.../runs/season-x-y/12345-slug`). */
data class RunRef(val season: String, val keystoneRunId: Long) {
    companion object {
        private val URL_PATTERN = Regex("""/(season-[^/]+)/(\d+)""")

        fun parse(url: String?): RunRef? {
            val match = URL_PATTERN.find(url ?: return null) ?: return null
            return RunRef(match.groupValues[1], match.groupValues[2].toLong())
        }
    }
}
