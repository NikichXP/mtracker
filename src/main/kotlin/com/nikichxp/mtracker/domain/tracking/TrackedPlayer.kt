package com.nikichxp.mtracker.domain.tracking

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.Instant

@Entity
@Table(name = "tracked_players")
class TrackedPlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var displayName: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tracked_player_character_keys", joinColumns = [JoinColumn(name = "tracked_player_id")])
    @Column(name = "character_key")
    @Fetch(FetchMode.SUBSELECT)
    var characterKeys: List<String> = emptyList(),
    var isFriend: Boolean = true,
    var addedAt: Instant = Instant.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackedPlayer) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "TrackedPlayer(id=$id, displayName='$displayName')"
}
