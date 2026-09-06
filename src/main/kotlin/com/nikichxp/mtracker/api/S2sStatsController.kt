package com.nikichxp.mtracker.api

import com.nikichxp.mtracker.web.StatsService
import com.nikichxp.mtracker.web.dto.PlayerDetailDto
import com.nikichxp.mtracker.web.dto.PlayerOverviewDto
import com.nikichxp.mtracker.web.dto.RecentRunDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Service-to-service counterpart of [StatsController], for other in-house services (currently
 * tg-bot) rather than the public dashboard. Protected by [S2sAuthFilter] instead of CORS, and kept
 * separate from `/api/v1/stats` so the public, unauthenticated dashboard endpoints are never
 * accidentally locked behind the shared S2S token (or vice versa).
 */
@RestController
@RequestMapping("/api/v1/s2s/stats")
class S2sStatsController(private val statsService: StatsService) {

    @GetMapping("/overview")
    fun overview(): List<PlayerOverviewDto> = statsService.overview()

    @GetMapping("/players/{playerKey}")
    fun playerDetail(@PathVariable playerKey: String): PlayerDetailDto =
        statsService.playerDetail(playerKey)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown player: $playerKey")

    @GetMapping("/players/{playerKey}/runs")
    fun recentRuns(@PathVariable playerKey: String): List<RecentRunDto> =
        statsService.recentRuns(playerKey)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown player: $playerKey")
}
