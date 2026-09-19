package com.nikichxp.mtracker.topgear

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.IRaiderIoService
import com.nikichxp.mtracker.raiderio.dto.MythicPlusStaticDataDto
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class SeasonResolverTest {

    private val objectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    private val raiderIoService: IRaiderIoService = mock()

    private fun staticData(): MythicPlusStaticDataDto {
        val json = javaClass.getResource("/stubs/static/mythic-plus-11.json")!!.readText()
        return objectMapper.readValue(json, MythicPlusStaticDataDto::class.java)
    }

    @Test
    fun `resolves the current main season`() = runBlocking<Unit> {
        whenever(raiderIoService.fetchMythicPlusStaticData(any())).thenReturn(staticData())
        val resolver = SeasonResolver(raiderIoService, MtrackerProperties())
        val now = Instant.parse("2026-09-19T00:00:00Z")
        assertThat(resolver.currentSeason(now)).isEqualTo("season-mn-2")
    }

    @Test
    fun `falls back to the latest main season when none is active`() = runBlocking<Unit> {
        whenever(raiderIoService.fetchMythicPlusStaticData(any())).thenReturn(staticData())
        val resolver = SeasonResolver(raiderIoService, MtrackerProperties())
        val now = Instant.parse("2031-01-01T00:00:00Z")
        assertThat(resolver.currentSeason(now)).isEqualTo("season-mn-2")
    }

    @Test
    fun `returns null when upstream fails`() = runBlocking<Unit> {
        whenever(raiderIoService.fetchMythicPlusStaticData(any())).thenReturn(null)
        val resolver = SeasonResolver(raiderIoService, MtrackerProperties())
        assertThat(resolver.currentSeason()).isNull()
    }
}
