package com.nikichxp.mtracker.wowhead

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.File

@Service
@ConditionalOnProperty(name = ["mtracker.wowhead.stub.enabled"], havingValue = "true")
class WowheadServiceStubImpl(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
) : IWowheadService {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun fetchItem(itemId: Int): WowheadItemDto? {
        val path = "stubs/wowhead/item-$itemId.xml"
        val content = readStubContent(path)
        if (content == null) {
            log.warn("No stub found at {}", path)
            return null
        }
        return WowheadItemXmlParser.parse(itemId, content)
    }

    override suspend fun fetchItemTooltip(itemId: Int, bonusIds: List<Int>): WowheadTooltipDto? {
        val path = "stubs/wowhead/tooltip-$itemId.json"
        val content = readStubContent(path)
        if (content == null) {
            log.warn("No stub found at {}", path)
            return null
        }
        return try {
            val node = objectMapper.readTree(content)
            WowheadTooltipParser.parse(itemId, node.get("name")?.asText(), node.get("tooltip")?.asText() ?: "")
        } catch (e: Exception) {
            log.error("Failed to deserialize stub $path", e)
            null
        }
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
