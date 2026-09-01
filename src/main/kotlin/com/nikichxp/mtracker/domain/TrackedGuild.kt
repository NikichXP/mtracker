package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * A WoW guild tracked via Raider.io: [com.nikichxp.mtracker.sync.RosterResolver] pulls its full
 * roster on every sync. Replaces the old `mtracker.guild` static YAML config - guilds are now
 * managed at runtime via [com.nikichxp.mtracker.web.GuildAdminController].
 *
 * Region is a single global setting (`mtracker.region` / `MTRACKER_REGION`), not per-guild.
 */
@Document(collection = "tracked_guilds")
@CompoundIndex(name = "guild_identity", def = "{'name': 1, 'realm': 1}", unique = true)
data class TrackedGuild(
    @Id val id: String? = null,
    val name: String,
    val realm: String,
    val addedAt: Instant = Instant.now(),
)
