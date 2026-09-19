package com.nikichxp.mtracker.cucumber

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nikichxp.mtracker.domain.character.CharacterRepository
import com.nikichxp.mtracker.domain.topgear.GearItemCatalogRepository
import com.nikichxp.mtracker.domain.topgear.GearItemStatsRepository
import com.nikichxp.mtracker.domain.topgear.GearSnapshotRepository
import com.nikichxp.mtracker.domain.player.PlayerRepository
import com.nikichxp.mtracker.domain.run.RunPlayerRepository
import com.nikichxp.mtracker.domain.run.RunRepository
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTaskRepository
import com.nikichxp.mtracker.domain.tracking.TrackedGuildRepository
import com.nikichxp.mtracker.domain.tracking.TrackedPlayerRepository
import com.nikichxp.mtracker.domain.weekly.WeeklySnapshotRepository
import com.nikichxp.mtracker.sync.SyncOrchestrator
import com.nikichxp.mtracker.web.dto.CharacterDto
import com.nikichxp.mtracker.web.dto.GuildRequest
import com.nikichxp.mtracker.web.dto.PlayerDetailDto
import com.nikichxp.mtracker.web.dto.PlayerOverviewDto
import com.nikichxp.mtracker.web.dto.RecentRunDto
import com.nikichxp.mtracker.web.dto.TrackedGuildDto
import com.nikichxp.mtracker.web.dto.TrackedPlayerDto
import com.nikichxp.mtracker.web.dto.TrackedPlayerRequest
import com.nikichxp.mtracker.web.dto.WeeklyPlayerStatsDto
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

class StepDefinitions(
    private val applicationContext: ApplicationContext,
    private val objectMapper: ObjectMapper,
    private val syncOrchestrator: SyncOrchestrator,
    private val trackedGuildRepository: TrackedGuildRepository,
    private val trackedPlayerRepository: TrackedPlayerRepository,
    private val playerRepository: PlayerRepository,
    private val characterRepository: CharacterRepository,
    private val weeklySnapshotRepository: WeeklySnapshotRepository,
    private val runRepository: RunRepository,
    private val runPlayerRepository: RunPlayerRepository,
    private val topPlayerScanTaskRepository: TopPlayerScanTaskRepository,
    private val gearSnapshotRepository: GearSnapshotRepository,
    private val gearItemCatalogRepository: GearItemCatalogRepository,
    private val gearItemStatsRepository: GearItemStatsRepository,
) {

    private val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToApplicationContext(applicationContext).build()
    }

    private var lastStatus: Int = 0
    private var lastResponseBody: String = ""
    private var createdGuildId: Long? = null
    private var createdPlayerId: Long? = null

    @Given("the database is cleaned")
    fun cleanDatabase() {
        gearSnapshotRepository.deleteAll()
        topPlayerScanTaskRepository.deleteAll()
        gearItemCatalogRepository.deleteAll()
        gearItemStatsRepository.deleteAll()
        runPlayerRepository.deleteAll()
        runRepository.deleteAll()
        weeklySnapshotRepository.deleteAll()
        playerRepository.deleteAll()
        characterRepository.deleteAll()
        trackedPlayerRepository.deleteAll()
        trackedGuildRepository.deleteAll()
    }

    @When("a user performs GET {string} without token")
    fun performGetWithoutToken(path: String) {
        val result = webTestClient.get()
            .uri(path)
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the response status should be {int}")
    fun assertResponseStatus(expectedStatus: Int) {
        assertThat(lastStatus).isEqualTo(expectedStatus)
    }

    @When("an admin creates tracked guild with name {string} and realm {string}")
    fun adminCreatesGuild(name: String, realm: String) {
        val result = webTestClient.post()
            .uri("/api/v1/admin/guilds")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(GuildRequest(name = name, realm = realm))
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
        if (lastStatus == 201) {
            val dto = objectMapper.readValue<TrackedGuildDto>(lastResponseBody)
            createdGuildId = dto.id
        }
    }

    @Then("the response json contains name {string} and realm {string}")
    fun assertResponseJsonNameAndRealm(name: String, realm: String) {
        val node = objectMapper.readTree(lastResponseBody)
        assertThat(node.get("name").asText()).isEqualTo(name)
        assertThat(node.get("realm").asText()).isEqualTo(realm)
    }

    @When("an admin gets all tracked guilds")
    fun adminGetsAllGuilds() {
        val result = webTestClient.get()
            .uri("/api/v1/admin/guilds")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the guild list contains {string}")
    fun assertGuildListContains(expected: String) {
        val list = objectMapper.readValue<List<TrackedGuildDto>>(lastResponseBody)
        val keys = list.map { "${it.name}-${it.realm}" }
        assertThat(keys).contains(expected)
    }

    @When("an admin deletes the created guild")
    fun adminDeletesGuild() {
        val id = requireNotNull(createdGuildId)
        val result = webTestClient.delete()
            .uri("/api/v1/admin/guilds/$id")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the guild list should not contain {string}")
    fun assertGuildListNotContains(expected: String) {
        val list = objectMapper.readValue<List<TrackedGuildDto>>(lastResponseBody)
        val keys = list.map { "${it.name}-${it.realm}" }
        assertThat(keys).doesNotContain(expected)
    }

    @When("an admin creates tracked player with displayName {string} characterKeys {string} and isFriend {word}")
    fun adminCreatesPlayer(displayName: String, characterKeysStr: String, isFriendStr: String) {
        val characterKeys = characterKeysStr.split(",").map { it.trim() }
        val isFriend = isFriendStr.toBoolean()
        val result = webTestClient.post()
            .uri("/api/v1/admin/tracked-players")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TrackedPlayerRequest(displayName = displayName, characterKeys = characterKeys, isFriend = isFriend))
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
        if (lastStatus == 201) {
            val dto = objectMapper.readValue<TrackedPlayerDto>(lastResponseBody)
            createdPlayerId = dto.id
        }
    }

    @Then("the response json contains displayName {string}")
    fun assertResponseJsonDisplayName(displayName: String) {
        val node = objectMapper.readTree(lastResponseBody)
        assertThat(node.get("displayName").asText()).isEqualTo(displayName)
    }

    @When("an admin gets all tracked players")
    fun adminGetsAllPlayers() {
        val result = webTestClient.get()
            .uri("/api/v1/admin/tracked-players")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the player list contains {string}")
    fun assertPlayerListContains(expectedDisplayName: String) {
        val list = objectMapper.readValue<List<TrackedPlayerDto>>(lastResponseBody)
        assertThat(list.map { it.displayName }).contains(expectedDisplayName)
    }

    @When("an admin deletes the created player")
    fun adminDeletesPlayer() {
        val id = requireNotNull(createdPlayerId)
        val result = webTestClient.delete()
            .uri("/api/v1/admin/tracked-players/$id")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @When("a sync is triggered")
    fun triggerSync() {
        runBlocking {
            syncOrchestrator.runFullSync()
        }
    }

    @Then("the sync finishes successfully")
    fun assertSyncSuccess() {
        assertThat(characterRepository.count()).isGreaterThan(0)
    }

    @When("a user gets player overview")
    fun userGetsOverview() {
        val result = webTestClient.get()
            .uri("/api/v1/stats/overview")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the overview contains player {string} with totalScore {double} weeklyRuns {int} maxItemLevel {double} role {string}")
    fun assertOverviewPlayer(playerKey: String, totalScore: Double, weeklyRuns: Int, maxItemLevel: Double, role: String) {
        val list = objectMapper.readValue<List<PlayerOverviewDto>>(lastResponseBody)
        val item = list.firstOrNull { it.playerKey == playerKey }
        assertThat(item).isNotNull
        assertThat(item!!.totalScore).isEqualTo(totalScore)
        assertThat(item.weeklyRunsCount).isEqualTo(weeklyRuns)
        assertThat(item.maxItemLevel).isEqualTo(maxItemLevel)
        assertThat(item.activeSpecRole).isEqualTo(role)
    }

    @Then("the overview contains player {string} with displayName {string} totalScore {double} weeklyRuns {int} maxItemLevel {double} characterCount {int}")
    fun assertOverviewPlayerWithCount(playerKey: String, displayName: String, totalScore: Double, weeklyRuns: Int, maxItemLevel: Double, characterCount: Int) {
        val list = objectMapper.readValue<List<PlayerOverviewDto>>(lastResponseBody)
        val item = list.firstOrNull { it.playerKey == playerKey }
        assertThat(item).isNotNull
        assertThat(item!!.displayName).isEqualTo(displayName)
        assertThat(item.totalScore).isEqualTo(totalScore)
        assertThat(item.weeklyRunsCount).isEqualTo(weeklyRuns)
        assertThat(item.maxItemLevel).isEqualTo(maxItemLevel)
        assertThat(item.characterCount).isEqualTo(characterCount)
    }

    @When("a user gets player details for {string}")
    fun userGetsPlayerDetails(playerKey: String) {
        val result = webTestClient.get()
            .uri("/api/v1/stats/players/$playerKey")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the player detail has {int} characters")
    fun assertPlayerDetailCharacterCount(count: Int) {
        val dto = objectMapper.readValue<PlayerDetailDto>(lastResponseBody)
        assertThat(dto.characters).hasSize(count)
    }

    @Then("character {string} has spec {string} role {string} score {double} and {int} weekly runs")
    fun assertCharacterDetail(characterKey: String, spec: String, role: String, score: Double, weeklyRuns: Int) {
        val dto = objectMapper.readValue<PlayerDetailDto>(lastResponseBody)
        val char = dto.characters.firstOrNull { it.characterKey == characterKey }
        assertThat(char).isNotNull
        assertThat(char!!.activeSpecName).isEqualTo(spec)
        assertThat(char.activeSpecRole).isEqualTo(role)
        assertThat(char.mythicPlusScore).isEqualTo(score)
        assertThat(char.weeklyRuns).hasSize(weeklyRuns)
    }

    @When("a user gets recent runs for {string}")
    fun userGetsRecentRuns(playerKey: String) {
        val result = webTestClient.get()
            .uri("/api/v1/stats/players/$playerKey/runs")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the recent runs response has {int} runs")
    fun assertRecentRunsCount(count: Int) {
        val list = objectMapper.readValue<List<RecentRunDto>>(lastResponseBody)
        assertThat(list).hasSize(count)
    }

    @Then("recent run {long} in season {string} has {int} roster members including a tracked player")
    fun assertRecentRunRoster(keystoneRunId: Long, season: String, rosterSize: Int) {
        val list = objectMapper.readValue<List<RecentRunDto>>(lastResponseBody)
        val run = list.firstOrNull { it.keystoneRunId == keystoneRunId && it.season == season }
        assertThat(run).isNotNull
        assertThat(run!!.roster).hasSize(rosterSize)
        assertThat(run.roster.any { it.isTrackedPlayer && it.playerKey != null }).isTrue()
    }

    @When("a user gets available weeks")
    fun userGetsAvailableWeeks() {
        val result = webTestClient.get()
            .uri("/api/v1/stats/weeks")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the list of weeks is not empty")
    fun assertWeeksNotEmpty() {
        val list = objectMapper.readValue<List<String>>(lastResponseBody)
        assertThat(list).isNotEmpty
    }

    @When("a user gets weekly stats")
    fun userGetsWeeklyStats() {
        val result = webTestClient.get()
            .uri("/api/v1/stats/weekly")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @Then("the weekly stats list contains player {string} with {int} runs and totalScore {double}")
    fun assertWeeklyStats(playerKey: String, runs: Int, score: Double) {
        val list = objectMapper.readValue<List<WeeklyPlayerStatsDto>>(lastResponseBody)
        val item = list.firstOrNull { it.playerKey == playerKey }
        assertThat(item).isNotNull
        assertThat(item!!.weeklyRunsCount).isEqualTo(runs)
        assertThat(item.totalScore).isEqualTo(score)
    }

    @Then("the stored run {long} in season {string} has {int} roster players")
    fun assertStoredRunRosterSize(keystoneRunId: Long, season: String, rosterSize: Int) {
        val run = runRepository.findBySeasonAndKeystoneRunId(season, keystoneRunId)
        assertThat(run).isNotNull
        assertThat(runPlayerRepository.findByRunIdIn(listOf(run!!.id!!))).hasSize(rosterSize)
    }

    @Then("the roster player {string} of run {long} in season {string} is linked to a tracked player")
    fun assertRosterPlayerLinked(characterKey: String, keystoneRunId: Long, season: String) {
        val member = findRosterPlayer(characterKey, keystoneRunId, season)
        assertThat(member.player).isNotNull
        assertThat(member.rioScore).isNotNull()
        assertThat(member.characterClass).isNotNull()
        assertThat(member.itemLevel).isNotNull()
    }

    @Then("the roster player {string} of run {long} in season {string} is not linked to a tracked player")
    fun assertRosterPlayerNotLinked(characterKey: String, keystoneRunId: Long, season: String) {
        val member = findRosterPlayer(characterKey, keystoneRunId, season)
        assertThat(member.player).isNull()
    }

    @Then("the overview contains player {string} with buddyScore {double}")
    fun assertOverviewBuddyScore(playerKey: String, buddyScore: Double) {
        val list = objectMapper.readValue<List<PlayerOverviewDto>>(lastResponseBody)
        val item = list.firstOrNull { it.playerKey == playerKey }
        assertThat(item).isNotNull
        assertThat(item!!.buddyScore).isEqualTo(buddyScore)
    }

    @Then("player {string} has rioScore {double} and next update scheduled")
    fun assertPlayerSchedule(playerKey: String, rioScore: Double) {
        val player = playerRepository.findByPlayerKey(playerKey)
        assertThat(player).isNotNull
        assertThat(player!!.rioScore).isEqualTo(rioScore)
        assertThat(player.nextUpdateAt).isNotNull()
        assertThat(player.nextUpdateAt).isAfter(Instant.now())
    }

    private fun findRosterPlayer(characterKey: String, keystoneRunId: Long, season: String) =
        runRepository.findBySeasonAndKeystoneRunId(season, keystoneRunId)
            .let { run ->
                assertThat(run).isNotNull
                runPlayerRepository.findByRunIdIn(listOf(run!!.id!!))
                    .firstOrNull { it.characterKey == characterKey }
                    .also { assertThat(it).isNotNull }!!
            }

    @When("an S2S client gets overview stats")
    fun s2sGetsOverview() {
        val result = webTestClient.get()
            .uri("/api/v1/s2s/stats/overview")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-s2s-token")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    @When("an S2S client gets player details for {string}")
    fun s2sGetsPlayerDetails(playerKey: String) {
        val result = webTestClient.get()
            .uri("/api/v1/s2s/stats/players/$playerKey")
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-s2s-token")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        captureResult(result)
    }

    private fun captureResult(result: EntityExchangeResult<String>) {
        lastStatus = result.status.value()
        lastResponseBody = result.responseBody ?: ""
    }
}
