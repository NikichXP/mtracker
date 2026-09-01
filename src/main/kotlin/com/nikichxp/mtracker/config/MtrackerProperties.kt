package com.nikichxp.mtracker.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * What to track (guilds, friends/alts) now lives in MongoDB - see [com.nikichxp.mtracker.domain.TrackedGuild]
 * and [com.nikichxp.mtracker.domain.TrackedPlayer], managed via the `/api/v1/admin` REST API.
 * This only holds process-level settings that aren't per-entity data.
 */
@ConfigurationProperties(prefix = "mtracker")
data class MtrackerProperties(
    /** Raider.io region for every tracked guild/player. Single-region deployment for now. */
    val region: String = "eu",
    val sync: Sync = Sync(),
    val raiderio: RaiderIo = RaiderIo(),
    val s2s: S2s = S2s(),
    val admin: Admin = Admin(),
) {
    data class Sync(val intervalHours: Long = 6, val requestDelayMs: Long = 250)
    data class RaiderIo(val baseUrl: String = "https://raider.io/api/v1")

    data class S2s(val token: String = "")

    /** Shared secret guarding the `/api/v1/admin` CRUD API (see [com.nikichxp.mtracker.web.AdminAuthFilter]). */
    data class Admin(val token: String = "")
}
