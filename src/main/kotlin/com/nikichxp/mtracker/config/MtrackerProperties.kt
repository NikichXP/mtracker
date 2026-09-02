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
    data class Sync(val intervalHours: Long = 6, val requestDelayMs: Long = 250)
    data class RaiderIo(val baseUrl: String = "https://raider.io/api/v1")

    data class S2s(val token: String = "")

    data class Admin(val token: String = "")
}
