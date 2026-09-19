package com.nikichxp.mtracker.domain.tracking

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
class TrackedGuild(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String = "",
    var realm: String = "",
    var addedAt: Instant = Instant.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackedGuild) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "TrackedGuild(id=$id, name='$name', realm='$realm')"
}
