package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.web.dto.PlayerDetailDto
import com.nikichxp.mtracker.web.dto.PlayerOverviewDto
import com.nikichxp.mtracker.web.dto.WeeklyPlayerStatsDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/** Read-only endpoints backing the mtracker dashboard frontend. */
@RestController
@RequestMapping("/api/v1/stats")
class StatsController(private val statsService: StatsService) {

    @GetMapping("/overview")
    fun overview(): List<PlayerOverviewDto> = statsService.overview()

    @GetMapping("/players/{playerKey}")
    fun playerDetail(@PathVariable playerKey: String): PlayerDetailDto =
        statsService.playerDetail(playerKey)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown player: $playerKey")

    @GetMapping("/weekly")
    fun weekly(@RequestParam(required = false) week: String?): List<WeeklyPlayerStatsDto> =
        statsService.weeklyStats(week)

    @GetMapping("/weeks")
    fun weeks(): List<String> = statsService.availableWeeks()
}
