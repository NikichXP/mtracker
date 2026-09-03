package com.nikichxp.mtracker.raiderio

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.dto.CharacterProfileDto
import com.nikichxp.mtracker.raiderio.dto.GuildMemberDto
import com.nikichxp.mtracker.raiderio.dto.GuildProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
    private val region = props.region

    private val characterFields = listOf(
        "gear",
        "guild",
        "mythic_plus_scores_by_season:current",
        "mythic_plus_ranks",
        "mythic_plus_recent_runs",
        "mythic_plus_best_runs",
        "mythic_plus_weekly_highest_level_runs",
    ).joinToString(",")

    override suspend fun fetchCharacterProfile(name: String, realm: String): CharacterProfileDto? {
        limiter.acquire()
        return try {
            val response = httpClient.get("${props.raiderio.baseUrl.trimEnd('/')}/characters/profile") {
                parameter("region", region)
                parameter("realm", realm)
                parameter("name", name)
                parameter("fields", characterFields)
            }
            if (response.status.isSuccess()) {
                response.body<CharacterProfileDto>()
            } else {
                log.warn("Failed to fetch character profile for {}-{}: {} {}", name, realm, response.status.value, response.status.description)
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch character profile for {}-{}: {}", name, realm, e.message)
            null
        }
    }

    override suspend fun fetchGuildRoster(guildName: String, guildRealm: String): List<GuildMemberDto> {
        limiter.acquire()
        return try {
            val response = httpClient.get("${props.raiderio.baseUrl.trimEnd('/')}/guilds/profile") {
                parameter("region", region)
                parameter("realm", guildRealm)
                parameter("name", guildName)
                parameter("fields", "members")
            }
            if (response.status.isSuccess()) {
                response.body<GuildProfileDto>().members
            } else {
                log.warn("Failed to fetch guild roster for {}-{}: {} {}", guildName, guildRealm, response.status.value, response.status.description)
                emptyList()
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch guild roster for {}-{}: {}", guildName, guildRealm, e.message)
            emptyList()
        }
    }
}
