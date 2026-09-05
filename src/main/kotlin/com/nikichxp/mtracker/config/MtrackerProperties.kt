package com.nikichxp.mtracker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtracker")
data class MtrackerProperties(
    val region: String = "eu",
    val sync: Sync = Sync(),
    val raiderio: RaiderIo = RaiderIo(),
    val s2s: S2s = S2s(),
    val admin: Admin = Admin(),
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

    data class RaiderIo(val baseUrl: String = "https://raider.io/api/v1")

    data class S2s(val token: String = "")

    data class Admin(val token: String = "")
}
