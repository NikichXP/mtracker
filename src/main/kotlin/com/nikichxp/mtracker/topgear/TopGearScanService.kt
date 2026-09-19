package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.topgear.GearEnchantment
import com.nikichxp.mtracker.domain.topgear.GearSnapshot
import com.nikichxp.mtracker.domain.topgear.GearSnapshotItem
import com.nikichxp.mtracker.domain.topgear.GearSnapshotRepository
import com.nikichxp.mtracker.domain.topgear.GearSocket
import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.domain.topgear.PartyMemberSpec
import com.nikichxp.mtracker.domain.topgear.StatType
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTask
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTaskRepository
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.KeystoneRunDto
import com.nikichxp.mtracker.raiderio.dto.RunDetailsDto
import com.nikichxp.mtracker.raiderio.dto.RunRosterMemberDto
import com.nikichxp.mtracker.sync.RunRef
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

@Service
class TopGearScanService(
    private val raiderIoService: IRaiderIoService,
    private val taskRepository: TopPlayerScanTaskRepository,
    private val snapshotRepository: GearSnapshotRepository,
    private val claimer: ScanTaskClaimer,
    private val gearItemCatalogService: GearItemCatalogService,
    private val gearSourceResolver: GearSourceResolver,
    private val gearStatsService: GearStatsService,
    private val primaryStatResolver: PrimaryStatResolver,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun scanDueTasks() {
        while (true) {
            val staleBefore = Instant.now().minus(props.topGear.claimTimeoutMinutes, ChronoUnit.MINUTES)
            val tasks = taskRepository.findClaimable(staleBefore, PageRequest.of(0, props.topGear.scanBatchSize))
            if (tasks.isEmpty()) {
                return
            }
            for (task in tasks) {
                val claimed = claimer.claim(task) ?: continue
                val result = runCatching { scanTask(claimed) }
                if (result.isSuccess) {
                    claimer.complete(claimed)
                } else {
                    log.warn(
                        "Scan task {}-{} spec {} failed (attempt {}): {}",
                        claimed.name, claimed.realmSlug, claimed.specName, claimed.attempts,
                        result.exceptionOrNull()?.message,
                    )
                    if (claimed.attempts >= props.topGear.maxScanAttempts) {
                        claimer.abandon(claimed)
                    } else {
                        claimer.release(claimed)
                    }
                }
            }
        }
    }

    suspend fun scanTask(task: TopPlayerScanTask) {
        val profile = raiderIoService.fetchGearProfile(task.name, task.realmSlug)
            ?: throw IllegalStateException("No gear profile for ${task.characterKey}")
        val importString = profile.talentLoadout
            ?.takeIf { it.loadoutSpecId == task.specId }
            ?.loadoutText
            ?: task.talentImportString
        val runs = profile.mythicPlusBestRuns
            .filter { it.spec?.id == task.specId }
            .sortedByDescending { it.score ?: 0.0 }
            .take(props.topGear.maxRunsPerCharacter)
        for (run in runs) {
            val ref = RunRef.parse(run.url) ?: continue
            if (snapshotRepository.existsBySeasonAndKeystoneRunIdAndCharacterKey(
                    ref.season, ref.keystoneRunId, task.characterKey
                )
            ) {
                continue
            }
            val details = raiderIoService.fetchRunDetails(ref.season, ref.keystoneRunId) ?: continue
            storeSnapshots(ref, run, details, task, importString)
        }
    }

    private suspend fun storeSnapshots(
        ref: RunRef,
        run: KeystoneRunDto,
        details: RunDetailsDto,
        task: TopPlayerScanTask,
        importString: String?,
    ) {
        for (member in details.roster) {
            val character = member.character ?: continue
            val name = character.name ?: continue
            val memberRealmSlug = character.realm?.slug ?: continue
            val memberKey = "$name-$memberRealmSlug"
            val isTaskCharacter = name.equals(task.name, ignoreCase = true) &&
                    memberRealmSlug.equals(task.realmSlug, ignoreCase = true)
            val memberTask = if (isTaskCharacter) {
                task
            } else {
                if (character.region?.slug?.equals(props.region, ignoreCase = true) != true) {
                    continue
                }
                val specId = character.spec?.id ?: continue
                taskRepository.findBySeasonAndRegionAndCharacterKeyAndSpecId(
                    task.season, props.region, memberKey, specId
                )?.takeIf { it.claimedAt == null } ?: continue
            }
            val memberImport = if (isTaskCharacter) importString else memberTask.talentImportString
            val snapshot = buildSnapshot(ref, run, details, member, task, memberKey, memberImport)
            claimer.saveSnapshot(snapshot)
        }
    }

    private suspend fun buildSnapshot(
        ref: RunRef,
        run: KeystoneRunDto,
        details: RunDetailsDto,
        member: RunRosterMemberDto,
        task: TopPlayerScanTask,
        memberKey: String,
        importString: String?,
    ): GearSnapshot {
        val character = member.character!!
        val spec = character.spec
        val primaryStat = primaryStatResolver.primaryFor(spec?.name, character.characterClass?.name)
            ?: StatType.AGILITY
        val equippedItems = member.items?.items ?: emptyMap()
        val snapshot = GearSnapshot(
            region = props.region,
            season = task.season,
            keystoneRunId = ref.keystoneRunId,
            characterKey = memberKey,
            name = character.name ?: "",
            realmSlug = character.realm?.slug ?: "",
            className = character.characterClass?.name ?: "",
            specId = spec?.id ?: 0,
            specName = spec?.name ?: "",
            specSlug = spec?.slug ?: "",
            role = member.role ?: spec?.role ?: "",
            dungeonName = details.dungeon?.name ?: run.dungeon ?: "",
            dungeonShortName = details.dungeon?.shortName ?: run.shortName,
            mythicLevel = details.mythicLevel ?: run.mythicLevel ?: 0,
            runScore = run.score ?: details.score,
            timed = (details.numChests ?: run.numKeystoneUpgrades ?: 0) > 0,
            numKeystoneUpgrades = details.numChests ?: run.numKeystoneUpgrades ?: 0,
            completedAt = details.completedAt ?: run.completedAt,
            itemLevelEquipped = member.items?.itemLevelEquipped,
            rioScore = member.ranks?.score,
            talentImportString = importString,
            capturedAt = Instant.now(),
            partySpecs = details.roster
                .filter { it !== member }
                .map { other ->
                    PartyMemberSpec(
                        specId = other.character?.spec?.id ?: 0,
                        specName = other.character?.spec?.name ?: "",
                        specSlug = other.character?.spec?.slug ?: "",
                        role = other.role ?: other.character?.spec?.role ?: "",
                        className = other.character?.characterClass?.name ?: "",
                    )
                },
        )
        val items = mutableListOf<GearSnapshotItem>()
        val stats = mutableMapOf<StatType, Int>()
        val sources = mutableSetOf<GearSource>()
        for ((slot, item) in equippedItems) {
            val catalog = gearItemCatalogService.resolve(item)
            val source = gearSourceResolver.effectiveSource(catalog, item)
            sources.add(source)
            stats.mergeAll(gearStatsService.statsFor(item, primaryStat))
            stats.mergeAll(gearStatsService.gemStatsFor(item.gems))
            items.add(
                GearSnapshotItem(
                    snapshot = snapshot,
                    slot = slot,
                    itemId = item.itemId,
                    itemLevel = item.itemLevel,
                    itemName = item.name ?: catalog.englishName,
                    itemQuality = item.itemQuality ?: catalog.quality,
                    tierSetId = item.tier,
                    source = source,
                    bonusIds = item.bonuses,
                    gems = item.gemsDetail.mapNotNull { it.id?.let { id -> GearSocket(id, it.name) } }
                        .ifEmpty { item.gems.map { GearSocket(it) } },
                    enchants = item.enchantsDetail.mapNotNull { it.id?.let { id -> GearEnchantment(id, it.name) } }
                        .ifEmpty { item.enchants.map { GearEnchantment(it) } },
                )
            )
        }
        snapshot.stats = stats
        snapshot.gearSources = sources
        items.forEach { item -> item.snapshot = snapshot }
        snapshot.items = items
        return snapshot
    }

    private fun MutableMap<StatType, Int>.mergeAll(other: Map<StatType, Int>) {
        other.forEach { (stat, value) -> merge(stat, value, Int::plus) }
    }
}
