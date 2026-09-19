package com.nikichxp.mtracker.raiderio

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.dto.CharacterProfileDto
import com.nikichxp.mtracker.raiderio.dto.GuildMemberDto
import com.nikichxp.mtracker.raiderio.dto.GuildProfileDto
import com.nikichxp.mtracker.raiderio.dto.MythicPlusStaticDataDto
import com.nikichxp.mtracker.raiderio.dto.RaidingStaticDataDto
import com.nikichxp.mtracker.raiderio.dto.RankingsBodyDto
import com.nikichxp.mtracker.raiderio.dto.RankingsResponseDto
import com.nikichxp.mtracker.raiderio.dto.RunDetailsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["mtracker.raiderio.stub.enabled"], havingValue = "false", matchIfMissing = true)
class RaiderIoServiceImpl(
    private val httpClient: HttpClient,
    private val props: MtrackerProperties,
    private val eventLimiter: EventLimiter
) : IRaiderIoService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val region get() = props.region

    private val characterFields = listOf(
        "gear",
        "guild",
        "mythic_plus_scores_by_season:current",
        "mythic_plus_ranks",
        "mythic_plus_recent_runs",
        "mythic_plus_weekly_highest_level_runs",
    ).joinToString(",")

    override suspend fun fetchCharacterProfile(name: String, realm: String): CharacterProfileDto? {
        return try {
            val response = invokeCall("${props.raiderio.baseUrl.trimEnd('/')}/characters/profile") {
                parameter("region", region)
                parameter("realm", realm)
                parameter("name", name)
                parameter("fields", characterFields)
            }
            if (response.status.isSuccess()) {
                response.body<CharacterProfileDto>()
            } else {
                log.warn(
                    "Failed to fetch character profile for {}-{}: {} {}",
                    name,
                    realm,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch character profile for {}-{}: {}", name, realm, e.message)
            null
        }
    }

    override suspend fun fetchGuildRoster(guildName: String, guildRealm: String): List<GuildMemberDto> {

        return try {
            val response = invokeCall("${props.raiderio.baseUrl.trimEnd('/')}/guilds/profile") {
                parameter("region", region)
                parameter("realm", guildRealm)
                parameter("name", guildName)
                parameter("fields", "members")
            }
            if (response.status.isSuccess()) {
                response.body<GuildProfileDto>().members
            } else {
                log.warn(
                    "Failed to fetch guild roster for {}-{}: {} {}",
                    guildName,
                    guildRealm,
                    response.status.value,
                    response.status.description
                )
                emptyList()
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch guild roster for {}-{}: {}", guildName, guildRealm, e.message)
            emptyList()
        }
    }

    override suspend fun fetchRunDetails(season: String, keystoneRunId: Long): RunDetailsDto? {
        return try {
            val response = invokeCall("${props.raiderio.baseUrl.trimEnd('/')}/mythic-plus/run-details") {
                parameter("season", season)
                parameter("id", keystoneRunId)
            }
            if (response.status.isSuccess()) {
                response.body<RunDetailsDto>()
            } else {
                log.warn(
                    "Failed to fetch run details for {}/{}: {} {}",
                    season,
                    keystoneRunId,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch run details for {}/{}: {}", season, keystoneRunId, e.message)
            null
        }
    }

    private val gearProfileFields = listOf(
        "gear",
        "talents",
        "mythic_plus_best_runs:all",
        "mythic_plus_scores_by_season:current",
    ).joinToString(",")

    override suspend fun fetchGearProfile(name: String, realm: String): CharacterProfileDto? {
        return try {
            val response = invokeCall("${props.raiderio.baseUrl.trimEnd('/')}/characters/profile") {
                parameter("region", region)
                parameter("realm", realm)
                parameter("name", name)
                parameter("fields", gearProfileFields)
            }
            if (response.status.isSuccess()) {
                response.body<CharacterProfileDto>()
            } else {
                log.warn(
                    "Failed to fetch gear profile for {}-{}: {} {}",
                    name,
                    realm,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch gear profile for {}-{}: {}", name, realm, e.message)
            null
        }
    }

    override suspend fun fetchSpecRankings(season: String, classSlug: String, page: Int): RankingsBodyDto? {
        return try {
            val response =
                invokeCall("${props.raiderio.rankingsBaseUrl.trimEnd('/')}/mythic-plus/rankings/characters") {
                    parameter("region", region)
                    parameter("season", season)
                    parameter("class", classSlug)
                    parameter("role", "all")
                    parameter("page", page)
                }
            if (response.status.isSuccess()) {
                response.body<RankingsResponseDto>().rankings
            } else {
                log.warn(
                    "Failed to fetch spec rankings for {}/{}/{}: {} {}",
                    season,
                    classSlug,
                    page,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch spec rankings for {}/{}/{}: {}", season, classSlug, page, e.message)
            null
        }
    }

    override suspend fun fetchMythicPlusStaticData(expansionId: Int): MythicPlusStaticDataDto? {
        return try {
            val response = invokeCall("${props.raiderio.baseUrl.trimEnd('/')}/mythic-plus/static-data") {
                parameter("expansion_id", expansionId)
            }
            if (response.status.isSuccess()) {
                response.body<MythicPlusStaticDataDto>()
            } else {
                log.warn(
                    "Failed to fetch mythic-plus static data for expansion {}: {} {}",
                    expansionId,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch mythic-plus static data for expansion {}: {}", expansionId, e.message)
            null
        }
    }

    override suspend fun fetchRaidingStaticData(expansionId: Int): RaidingStaticDataDto? {
        return try {
            val response = invokeCall("${props.raiderio.baseUrl.trimEnd('/')}/raiding/static-data") {
                parameter("expansion_id", expansionId)
            }
            if (response.status.isSuccess()) {
                response.body<RaidingStaticDataDto>()
            } else {
                log.warn(
                    "Failed to fetch raiding static data for expansion {}: {} {}",
                    expansionId,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch raiding static data for expansion {}: {}", expansionId, e.message)
            null
        }
    }

    private suspend fun invokeCall(url: String, block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return eventLimiter.invoke(SYNC_KEY) {
            httpClient.get(url, block)
        }
    }

    companion object {
        private const val SYNC_KEY = "raider.io"
    }
}
