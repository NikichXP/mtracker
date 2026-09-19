package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.StaticSeasonDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class SeasonResolver(
    private val raiderIoService: IRaiderIoService,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val mutex = Mutex()
    private var cached: Pair<String, Instant>? = null

    suspend fun currentSeason(now: Instant = Instant.now()): String? {
        cached?.let { (slug, fetchedAt) ->
            if (fetchedAt.plus(CACHE_TTL).isAfter(now)) {
                return slug
            }
        }
        return mutex.withLock {
            cached?.let { (slug, fetchedAt) ->
                if (fetchedAt.plus(CACHE_TTL).isAfter(now)) {
                    return@withLock slug
                }
            }
            val slug = resolveSeason(now)
            if (slug != null) {
                cached = slug to now
            }
            slug
        }
    }

    private suspend fun resolveSeason(now: Instant): String? {
        val data = raiderIoService.fetchMythicPlusStaticData(props.topGear.expansionId)
        if (data == null) {
            log.warn("Failed to fetch mythic-plus static data, cannot resolve current season")
            return null
        }
        val mainSeasons = data.seasons.filter { it.isMainSeason }
        val current = mainSeasons.firstOrNull { season ->
            val start = season.starts[props.region]
            val end = season.ends[props.region]
            start != null && end != null && !start.isAfter(now) && end.isAfter(now)
        }
        val resolved = current ?: mainSeasons.maxByOrNull { it.starts[props.region] ?: Instant.EPOCH }
        if (resolved == null) {
            log.warn("No main season found in mythic-plus static data")
            return null
        }
        return resolved.slug
    }

    companion object {
        private val CACHE_TTL: Duration = Duration.ofHours(1)
    }
}
