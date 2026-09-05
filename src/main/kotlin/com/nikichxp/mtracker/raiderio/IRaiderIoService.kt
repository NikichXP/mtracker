package com.nikichxp.mtracker.raiderio

import com.nikichxp.mtracker.raiderio.dto.CharacterProfileDto
import com.nikichxp.mtracker.raiderio.dto.GuildMemberDto
import com.nikichxp.mtracker.raiderio.dto.RunDetailsDto

interface IRaiderIoService {
    suspend fun fetchCharacterProfile(name: String, realm: String): CharacterProfileDto?
    suspend fun fetchGuildRoster(guildName: String, guildRealm: String): List<GuildMemberDto>
    suspend fun fetchRunDetails(season: String, keystoneRunId: Long): RunDetailsDto?
}
