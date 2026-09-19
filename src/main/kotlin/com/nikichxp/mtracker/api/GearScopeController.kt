package com.nikichxp.mtracker.api

import com.nikichxp.mtracker.web.GearScopeService
import com.nikichxp.mtracker.web.dto.SpecGearReportDto
import com.nikichxp.mtracker.web.dto.SpecOverviewDto
import com.nikichxp.mtracker.web.dto.SpecSummaryDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/gearscope")
class GearScopeController(private val gearScopeService: GearScopeService) {

    @GetMapping("/specs")
    fun specs(): List<SpecOverviewDto> = gearScopeService.specOverview()

    @GetMapping("/specs/{specId}/items")
    fun specItems(
        @PathVariable specId: Int,
        @RequestParam(required = false, defaultValue = "false") excludeRaid: Boolean,
        @RequestParam(required = false) limit: Int?,
    ): SpecGearReportDto = gearScopeService.specItems(specId, excludeRaid, limit)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown spec: $specId")

    @GetMapping("/specs/{specId}/summary")
    fun specSummary(
        @PathVariable specId: Int,
        @RequestParam(required = false, defaultValue = "false") excludeRaid: Boolean,
    ): SpecSummaryDto = gearScopeService.specSummary(specId, excludeRaid)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown spec: $specId")
}
