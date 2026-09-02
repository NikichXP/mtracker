package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "tracked_guilds")
@CompoundIndex(name = "guild_identity", def = "{'name': 1, 'realm': 1}", unique = true)
data class TrackedGuild(
    @Id val id: String? = null,
    val name: String,
    val realm: String,
    val addedAt: Instant = Instant.now(),
)
