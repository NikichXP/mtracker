package com.nikichxp.mtracker.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "tracked_guilds",
    uniqueConstraints = [UniqueConstraint(name = "guild_identity", columnNames = ["name", "realm"])]
)
data class TrackedGuild(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String = "",
    val realm: String = "",
    val addedAt: Instant = Instant.now(),
)
