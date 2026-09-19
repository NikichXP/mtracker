package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTask
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTaskRepository
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.RankedCharacterDto
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.min

@Service
class TopPlayerScanTaskProducer(
    private val raiderIoService: IRaiderIoService,
    private val seasonResolver: SeasonResolver,
    private val repository: TopPlayerScanTaskRepository,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val classSlugs = listOf(
        "death-knight", "demon-hunter", "druid", "evoker", "hunter", "mage", "monk",
        "paladin", "priest", "rogue", "shaman", "warlock", "warrior",
    )

    suspend fun produceTasks() {
        if (!props.topGear.enabled) {
            return
        }
        val season = seasonResolver.currentSeason()
        if (season == null) {
            log.warn("Cannot produce top-player scan tasks: current season unresolved")
            return
        }
        for (classSlug in classSlugs) {
            try {
                produceForClass(season, classSlug)
            } catch (e: Exception) {
                log.error("Failed to produce scan tasks for class {}", classSlug, e)
            }
        }
    }

    private suspend fun produceForClass(season: String, classSlug: String) {
        val bySpec = LinkedHashMap<Int, MutableList<RankedCharacterDto>>()
        var classPopulation = 0
        var totalScanned = 0
        var page = 0
        while (true) {
            val body = raiderIoService.fetchSpecRankings(season, classSlug, page)
            val entries = body?.rankedCharacters
            if (entries.isNullOrEmpty()) {
                break
            }
            if (page == 0) {
                val ui = body.ui
                classPopulation = ((ui?.lastPage ?: 0) + 1) * (ui?.pageSize ?: 100)
            }
            totalScanned += entries.size
            for (entry in entries) {
                val specId = entry.character?.spec?.id ?: continue
                bySpec.getOrPut(specId) { mutableListOf() }.add(entry)
            }
            val lastPage = body.ui?.lastPage ?: page
            if (page + 1 >= min(props.topGear.maxPagesPerClass, lastPage + 1)) {
                break
            }
            val allSpecsSaturated = page + 1 >= props.topGear.minPagesPerClass &&
                bySpec.values.all { it.size >= props.topGear.maxPerSpec }
            if (allSpecsSaturated) {
                break
            }
            page++
        }
        if (totalScanned == 0) {
            log.warn("No rankings entries found for class {} in season {}", classSlug, season)
            return
        }
        var created = 0
        for ((_, specEntries) in bySpec) {
            val estimatedSpecPopulation = classPopulation * specEntries.size / totalScanned
            val percentileCap = ceil(props.topGear.percentile / 100.0 * estimatedSpecPopulation).toInt()
            val cap = min(props.topGear.maxPerSpec, percentileCap).coerceAtLeast(1)
            for (entry in specEntries.take(cap)) {
                if (createTask(season, entry)) {
                    created++
                }
            }
        }
        log.info(
            "Produced {} scan tasks for class {} ({} pages, {} specs, season {})",
            created, classSlug, page + 1, bySpec.size, season,
        )
    }

    private fun createTask(season: String, entry: RankedCharacterDto): Boolean {
        val character = entry.character ?: return false
        val spec = character.spec ?: return false
        val name = character.name ?: return false
        val realmSlug = character.realm?.slug ?: return false
        val specId = spec.id ?: return false
        val characterKey = "$name-$realmSlug"
        if (repository.existsBySeasonAndRegionAndCharacterKeyAndSpecId(
                season, props.region, characterKey, specId
            )
        ) {
            return false
        }
        val task = TopPlayerScanTask(
            region = props.region,
            season = season,
            characterKey = characterKey,
            name = name,
            realmSlug = realmSlug,
            className = character.characterClass?.name ?: "",
            specId = specId,
            specName = spec.name ?: "",
            specSlug = spec.slug ?: "",
            role = spec.role ?: "",
            rioScore = entry.score ?: 0.0,
            rank = entry.rank ?: 0,
            talentImportString = character.talentLoadoutText,
            guildName = entry.guild?.name,
            createdAt = Instant.now(),
        )
        return try {
            repository.save(task)
            true
        } catch (e: DataIntegrityViolationException) {
            false
        }
    }
}
