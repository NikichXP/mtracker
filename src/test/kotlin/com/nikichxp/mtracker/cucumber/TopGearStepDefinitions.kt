package com.nikichxp.mtracker.cucumber

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.mtracker.domain.topgear.GearItemCatalogRepository
import com.nikichxp.mtracker.domain.topgear.GearSnapshot
import com.nikichxp.mtracker.domain.topgear.GearSnapshotItem
import com.nikichxp.mtracker.domain.topgear.GearSnapshotRepository
import com.nikichxp.mtracker.domain.topgear.GearSource
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTask
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTaskRepository
import com.nikichxp.mtracker.topgear.TopGearCleanupService
import com.nikichxp.mtracker.topgear.TopGearScanService
import com.nikichxp.mtracker.topgear.TopPlayerScanTaskProducer
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant
import java.time.temporal.ChronoUnit

class TopGearStepDefinitions(
    private val applicationContext: ApplicationContext,
    private val objectMapper: ObjectMapper,
    private val producer: TopPlayerScanTaskProducer,
    private val scanService: TopGearScanService,
    private val cleanupService: TopGearCleanupService,
    private val taskRepository: TopPlayerScanTaskRepository,
    private val snapshotRepository: GearSnapshotRepository,
    private val catalogRepository: GearItemCatalogRepository,
) {

    private val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToApplicationContext(applicationContext).build()
    }

    private var lastStatus: Int = 0
    private var lastResponseBody: String = ""

    @When("the top gear producer runs")
    fun runProducer() = runBlocking {
        producer.produceTasks()
    }

    @When("the top gear scan runs")
    fun runScan() = runBlocking {
        scanService.scanDueTasks()
    }

    @When("the top gear cleanup runs")
    fun runCleanup() {
        cleanupService.purgeDeprecated()
    }

    @Given("a top gear scan task exists for {string} realm {string} spec {int} named {string} scoring {double}")
    fun createScanTask(name: String, realm: String, specId: Int, specName: String, score: Double) {
        taskRepository.save(
            TopPlayerScanTask(
                region = "eu",
                season = "season-mn-2",
                characterKey = "$name-$realm",
                name = name,
                realmSlug = realm,
                className = "Monk",
                specId = specId,
                specName = specName,
                specSlug = specName.lowercase(),
                role = "dps",
                rioScore = score,
                rank = 1,
                talentImportString = "TASK_TALENT",
                createdAt = Instant.now(),
            )
        )
    }

    @Given("the scan task for {string} spec {int} is claimed {int} minutes ago")
    fun claimTask(name: String, specId: Int, minutesAgo: Int) {
        val task = findTask(name, specId)
        task.claimedAt = Instant.now().minus(minutesAgo.toLong(), ChronoUnit.MINUTES)
        task.claimedBy = "other-instance"
        taskRepository.save(task)
    }

    @Given("a gear item catalog entry exists for item {int}")
    fun createCatalogEntry(itemId: Int) {
        catalogRepository.save(
            com.nikichxp.mtracker.domain.topgear.GearItemCatalog(
                itemId = itemId,
                englishName = "Cached Item",
                source = GearSource.KEYS,
                fetchedAt = Instant.now(),
            )
        )
    }

    @Given("a gear snapshot exists for {string} captured {int} hours ago")
    fun createOldSnapshot(characterKey: String, hoursAgo: Int) {
        val snapshot = GearSnapshot(
            region = "eu",
            season = "season-mn-2",
            keystoneRunId = (9000 + hoursAgo).toLong(),
            characterKey = characterKey,
            name = characterKey.substringBefore("-"),
            realmSlug = characterKey.substringAfter("-"),
            className = "Monk",
            specId = 269,
            specName = "Windwalker",
            specSlug = "windwalker",
            role = "dps",
            dungeonName = "Den of Nalorakk",
            mythicLevel = 22,
            capturedAt = Instant.now().minus(hoursAgo.toLong(), ChronoUnit.HOURS),
        )
        snapshot.items = listOf(
            GearSnapshotItem(snapshot = snapshot, slot = "head", itemId = 271517, itemLevel = 250)
        )
        snapshotRepository.save(snapshot)
    }

    @Then("{int} top gear scan tasks exist")
    fun assertTaskCount(count: Int) {
        assertThat(taskRepository.count()).isEqualTo(count.toLong())
    }

    @Then("the character {string} has {int} scan tasks for different specs")
    fun assertDoubleSpecTasks(characterKey: String, count: Int) {
        val tasks = taskRepository.findAll().filter { it.characterKey == characterKey }
        assertThat(tasks).hasSize(count)
        assertThat(tasks.map { it.specId }.distinct()).hasSize(count)
    }

    @Then("a scan task exists for {string} spec {int}")
    fun assertTaskExists(characterKey: String, specId: Int) {
        val task = taskRepository.findBySeasonAndRegionAndCharacterKeyAndSpecId(
            "season-mn-2", "eu", characterKey, specId
        )
        assertThat(task).isNotNull
    }

    @Then("no scan task exists for {string}")
    fun assertTaskGone(characterKey: String) {
        assertThat(taskRepository.findAll().none { it.characterKey == characterKey }).isTrue()
    }

    @Then("{int} gear snapshots exist")
    fun assertSnapshotCount(count: Int) {
        assertThat(snapshotRepository.count()).isEqualTo(count.toLong())
    }

    @Then("the gear snapshot for {string} run {long} has {int} items, {int} party specs and sources {string}")
    fun assertSnapshotShape(characterKey: String, keystoneRunId: Long, itemCount: Int, partyCount: Int, sources: String) {
        val snapshot = findSnapshot(characterKey, keystoneRunId)
        assertThat(snapshot.items).hasSize(itemCount)
        assertThat(snapshot.partySpecs).hasSize(partyCount)
        val expected = sources.split(",").map { GearSource.valueOf(it.trim()) }.toSet()
        assertThat(snapshot.gearSources).isEqualTo(expected)
        assertThat(snapshot.stats).isNotEmpty
    }

    @Then("the gear snapshot for {string} run {long} uses talent import {string}")
    fun assertSnapshotTalent(characterKey: String, keystoneRunId: Long, importString: String) {
        assertThat(findSnapshot(characterKey, keystoneRunId).talentImportString).isEqualTo(importString)
    }

    @Then("{int} gear item catalog entries exist")
    fun assertCatalogCount(count: Int) {
        assertThat(catalogRepository.count()).isEqualTo(count.toLong())
    }

    @When("a user gets gearscope specs")
    fun getSpecs() {
        capture(webTestClient.get().uri("/api/v1/gearscope/specs").exchange()
            .expectBody(String::class.java).returnResult())
    }

    @When("a user gets gearscope items for spec {int}")
    fun getSpecItems(specId: Int) {
        capture(webTestClient.get().uri("/api/v1/gearscope/specs/$specId/items").exchange()
            .expectBody(String::class.java).returnResult())
    }

    @When("a user gets gearscope items for spec {int} excluding raid")
    fun getSpecItemsExcludingRaid(specId: Int) {
        capture(webTestClient.get().uri("/api/v1/gearscope/specs/$specId/items?excludeRaid=true").exchange()
            .expectBody(String::class.java).returnResult())
    }

    @Then("the gearscope specs response contains spec {int} with parseCount {int} and characterCount {int}")
    fun assertSpecOverview(specId: Int, parseCount: Int, characterCount: Int) {
        assertThat(lastStatus).isEqualTo(200)
        val node = objectMapper.readTree(lastResponseBody)
        val spec = node.firstOrNull { it.get("specId").asInt() == specId }
        assertThat(spec).isNotNull
        assertThat(spec!!.get("parseCount").asInt()).isEqualTo(parseCount)
        assertThat(spec.get("characterCount").asInt()).isEqualTo(characterCount)
    }

    @Then("the gearscope items response has item {int} in slot {string} with source {string}")
    fun assertSlotItem(itemId: Int, slot: String, source: String) {
        assertThat(lastStatus).isEqualTo(200)
        val item = slotItem(slot, itemId)
        assertThat(item).isNotNull
        assertThat(item!!.get("source").asText()).isEqualTo(source)
        assertThat(item.get("stats")).isNotNull
    }

    @Then("the gearscope items response contains no RAID or SET items")
    fun assertNoRaidOrSetItems() {
        assertThat(lastStatus).isEqualTo(200)
        val node = objectMapper.readTree(lastResponseBody)
        val sources = node.get("slots").flatMap { it.get("topItems") }.map { it.get("source").asText() }
        assertThat(sources).doesNotContain("RAID", "SET")
        assertThat(sources).isNotEmpty
    }

    private fun slotItem(slot: String, itemId: Int): JsonNode? {
        val node = objectMapper.readTree(lastResponseBody)
        return node.get("slots")
            .firstOrNull { it.get("slot").asText() == slot }
            ?.get("topItems")
            ?.firstOrNull { it.get("itemId").asInt() == itemId }
    }

    private fun findTask(name: String, specId: Int): TopPlayerScanTask {
        val task = taskRepository.findAll().firstOrNull { it.name == name && it.specId == specId }
        assertThat(task).isNotNull
        return task!!
    }

    private fun findSnapshot(characterKey: String, keystoneRunId: Long): GearSnapshot {
        val snapshot = snapshotRepository.findAll()
            .firstOrNull { it.characterKey == characterKey && it.keystoneRunId == keystoneRunId }
        assertThat(snapshot).isNotNull
        return snapshot!!
    }

    private fun capture(result: EntityExchangeResult<String>) {
        lastStatus = result.status.value()
        lastResponseBody = result.responseBody ?: ""
    }
}
