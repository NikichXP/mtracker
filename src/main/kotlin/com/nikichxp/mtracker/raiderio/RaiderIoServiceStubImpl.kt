package com.nikichxp.mtracker.raiderio

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nikichxp.mtracker.raiderio.dto.CharacterProfileDto
import com.nikichxp.mtracker.raiderio.dto.GuildMemberDto
import com.nikichxp.mtracker.raiderio.dto.GuildProfileDto
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.File

@Service
@ConditionalOnProperty(name = ["mtracker.raiderio.stub.enabled"], havingValue = "true")
class RaiderIoServiceStubImpl(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
) : IRaiderIoService {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun fetchCharacterProfile(name: String, realm: String): CharacterProfileDto? {
        val candidates = listOf(
            "stubs/characters/$name-$realm.json",
            "stubs/characters/${name.lowercase()}-${realm.lowercase()}.json",
            "stubs/characters/$name.json",
            "stubs/characters/${name.lowercase()}.json",
        )
        for (candidate in candidates) {
            val content = readStubContent(candidate)
            if (content != null) {
                return try {
                    objectMapper.readValue<CharacterProfileDto>(content)
                } catch (e: Exception) {
                    log.error("Failed to deserialize stub $candidate", e)
                    null
                }
            }
        }
        log.warn("No stub found for character {}-{} in candidates: {}", name, realm, candidates)
        return null
    }

    override suspend fun fetchGuildRoster(guildName: String, guildRealm: String): List<GuildMemberDto> {
        val candidates = listOf(
            "stubs/guilds/$guildName-$guildRealm.json",
            "stubs/guilds/${guildName.lowercase()}-${guildRealm.lowercase()}.json",
            "stubs/guilds/$guildName.json",
            "stubs/guilds/${guildName.lowercase()}.json",
        )
        for (candidate in candidates) {
            val content = readStubContent(candidate)
            if (content != null) {
                return try {
                    objectMapper.readValue<GuildProfileDto>(content).members
                } catch (e: Exception) {
                    log.error("Failed to deserialize stub $candidate", e)
                    emptyList()
                }
            }
        }
        log.warn("No stub found for guild {}-{} in candidates: {}", guildName, guildRealm, candidates)
        return emptyList()
    }

    private fun readStubContent(path: String): String? {
        val classPathResource = resourceLoader.getResource("classpath:$path")
        if (classPathResource.exists()) {
            return classPathResource.inputStream.bufferedReader().use { it.readText() }
        }
        val file = File(path)
        if (file.exists()) {
            return file.readText()
        }
        val srcResourceFile = File("src/main/resources/$path")
        if (srcResourceFile.exists()) {
            return srcResourceFile.readText()
        }
        val srcTestResourceFile = File("src/test/resources/$path")
        if (srcTestResourceFile.exists()) {
            return srcTestResourceFile.readText()
        }
        return null
    }
}
