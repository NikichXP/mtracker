package com.nikichxp.mtracker.wowhead

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.raiderio.EventLimiter
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["mtracker.wowhead.stub.enabled"], havingValue = "false", matchIfMissing = true)
class WowheadServiceImpl(
    private val httpClient: HttpClient,
    private val props: MtrackerProperties,
    private val eventLimiter: EventLimiter,
    private val objectMapper: ObjectMapper,
) : IWowheadService {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun fetchItem(itemId: Int): WowheadItemDto? {
        return try {
            val response = eventLimiter.invoke(SYNC_KEY) {
                httpClient.get("${props.wowhead.baseUrl.trimEnd('/')}/item=$itemId&xml")
            }
            if (response.status.isSuccess()) {
                WowheadItemXmlParser.parse(itemId, response.bodyAsText())
            } else {
                log.warn("Failed to fetch wowhead item {}: {} {}", itemId, response.status.value, response.status.description)
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch wowhead item {}: {}", itemId, e.message)
            null
        }
    }

    override suspend fun fetchItemTooltip(itemId: Int, bonusIds: List<Int>): WowheadTooltipDto? {
        return try {
            val response = eventLimiter.invoke(SYNC_KEY) {
                httpClient.get("${props.wowhead.tooltipBaseUrl.trimEnd('/')}/tooltip/item/$itemId") {
                    parameter("dataEnv", 1)
                    parameter("locale", 0)
                    if (bonusIds.isNotEmpty()) {
                        parameter("bonus", bonusIds.joinToString(":"))
                    }
                }
            }
            if (response.status.isSuccess()) {
                val node = objectMapper.readTree(response.bodyAsText())
                WowheadTooltipParser.parse(itemId, node.get("name")?.asText(), node.get("tooltip")?.asText() ?: "")
            } else {
                log.warn(
                    "Failed to fetch wowhead tooltip for {}: {} {}",
                    itemId,
                    response.status.value,
                    response.status.description
                )
                null
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch wowhead tooltip for {}: {}", itemId, e.message)
            null
        }
    }

    companion object {
        private const val SYNC_KEY = "wowhead"
    }
}
