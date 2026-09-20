package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Locale

@Service
class RaidEncounterCatalog(
    private val raiderIoService: IRaiderIoService,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val mutex = Mutex()
    private var cached: Pair<Map<String, String>, Instant>? = null
    private var failedUntil: Instant? = null

    suspend fun isRaidEncounter(name: String?): Boolean {
        if (name == null) {
            return false
        }
        return normalize(name) in encountersByRaid()
    }

    suspend fun raidNameFor(bossName: String?): String? {
        if (bossName == null) {
            return null
        }
        return encountersByRaid()[normalize(bossName)]
    }

    private suspend fun encountersByRaid(): Map<String, String> {
        val now = Instant.now()
        cached?.let { (names, fetchedAt) ->
            if (fetchedAt.plus(CACHE_TTL).isAfter(now)) {
                return names
            }
        }
        return mutex.withLock {
            val refreshedAt = Instant.now()
            cached?.let { (names, fetchedAt) ->
                if (fetchedAt.plus(CACHE_TTL).isAfter(refreshedAt)) {
                    return@withLock names
                }
            }
            failedUntil?.let {
                if (it.isAfter(refreshedAt)) {
                    return@withLock cached?.first ?: emptyMap()
                }
            }
            val data = raiderIoService.fetchRaidingStaticData(props.topGear.expansionId)
            if (data == null) {
                log.warn("Failed to fetch raiding static data, raid encounter catalog unavailable")
                failedUntil = refreshedAt.plus(NEGATIVE_CACHE_TTL)
                return@withLock cached?.first ?: emptyMap()
            }
            failedUntil = null
            val names = data.raids
                .flatMap { raid -> raid.encounters.mapNotNull { it.name }.map { normalize(it) to (raid.name ?: "") } }
                .toMap()
            cached = names to refreshedAt
            names
        }
    }

    private fun normalize(name: String): String = name.trim().lowercase(Locale.ROOT)

    companion object {
        private val CACHE_TTL: Duration = Duration.ofHours(6)
        private val NEGATIVE_CACHE_TTL: Duration = Duration.ofMinutes(1)
    }
}
