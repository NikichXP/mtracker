package com.nikichxp.mtracker.raiderio

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.dto.CharacterProfileDto
import com.nikichxp.mtracker.raiderio.dto.GuildMemberDto
import com.nikichxp.mtracker.raiderio.dto.GuildProfileDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class RaiderIoClient(
    private val webClient: WebClient,
    props: MtrackerProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val region = props.region
    private val limiter = RaiderIoRateLimiter(props.sync.requestDelayMs)

    private val characterFields = listOf(
        "gear",
        "guild",
        "mythic_plus_scores_by_season:current",
        "mythic_plus_ranks",
        "mythic_plus_recent_runs",
        "mythic_plus_best_runs",
        "mythic_plus_weekly_highest_level_runs",
    ).joinToString(",")

    fun fetchCharacterProfile(name: String, realm: String): CharacterProfileDto? {
        limiter.acquire()
        return try {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/characters/profile")
                        .queryParam("region", region)
                        .queryParam("realm", realm)
                        .queryParam("name", name)
                        .queryParam("fields", characterFields)
                        .build()
                }
                .retrieve()
                .bodyToMono(CharacterProfileDto::class.java)
                .block()
        } catch (e: WebClientResponseException) {
            log.warn("Failed to fetch character profile for {}-{}: {} {}", name, realm, e.statusCode, e.message)
            null
        } catch (e: Exception) {
            log.warn("Failed to fetch character profile for {}-{}: {}", name, realm, e.message)
            null
        }
    }

    fun fetchGuildRoster(guildName: String, guildRealm: String): List<GuildMemberDto> {
        limiter.acquire()
        return try {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/guilds/profile")
                        .queryParam("region", region)
                        .queryParam("realm", guildRealm)
                        .queryParam("name", guildName)
                        .queryParam("fields", "members")
                        .build()
                }
                .retrieve()
                .bodyToMono(GuildProfileDto::class.java)
                .block()
                ?.members
                ?: emptyList()
        } catch (e: WebClientResponseException) {
            log.warn("Failed to fetch guild roster for {}-{}: {} {}", guildName, guildRealm, e.statusCode, e.message)
            emptyList()
        } catch (e: Exception) {
            log.warn("Failed to fetch guild roster for {}-{}: {}", guildName, guildRealm, e.message)
            emptyList()
        }
    }
}
