package com.nikichxp.mtracker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtracker")
data class MtrackerProperties(
    val region: String = "eu",
    val sync: Sync = Sync(),
    val raiderio: RaiderIo = RaiderIo(),
    val s2s: S2s = S2s(),
    val admin: Admin = Admin(),
    val topGear: TopGear = TopGear(),
    val wowhead: Wowhead = Wowhead(),
) {

    data class Sync(
        /** Interval between full syncs (roster rebuild + due player updates). */
        val intervalHours: Long = 6,
        /** How often the scheduler polls for players whose nextUpdateAt is due. */
        val pollIntervalMs: Long = 60_000,
        /** How many due players are fetched per batch while draining the update queue. */
        val batchSize: Int = 10,
        val defaultDelay: Long = 100,
        val requestDelay: Map<String, Long> = emptyMap()
    )

    data class RaiderIo(
        val baseUrl: String = "https://raider.io/api/v1",
        val rankingsBaseUrl: String = "https://raider.io/api",
    )

    data class S2s(val token: String = "")

    data class Admin(val token: String = "")

    data class TopGear(
        val enabled: Boolean = true,
        val schedulerEnabled: Boolean = true,
        val expansionId: Int = 11,
        val percentile: Double = 5.0,
        val maxPerSpec: Int = 200,
        val maxPagesPerClass: Int = 60,
        val minPagesPerClass: Int = 10,
        val topItemsPerSlot: Int = 5,
        val producerIntervalHours: Long = 24,
        val scanPollIntervalMs: Long = 30_000,
        val scanBatchSize: Int = 20,
        val maxRunsPerCharacter: Int = 8,
        val maxScanAttempts: Int = 3,
        val claimTimeoutMinutes: Long = 15,
        val retentionHours: Long = 72,
        val cleanupIntervalHours: Long = 1,
    )

    data class Wowhead(
        val baseUrl: String = "https://www.wowhead.com",
        val tooltipBaseUrl: String = "https://nether.wowhead.com",
    )
}
