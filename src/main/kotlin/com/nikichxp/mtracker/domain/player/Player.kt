package com.nikichxp.mtracker.domain.player

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.Instant

@Entity
@Table(
    name = "players",
    indexes = [
        Index(name = "idx_players_player_key", columnList = "playerKey", unique = true),
        Index(name = "idx_players_next_update_at", columnList = "nextUpdateAt")
    ]
)
class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var playerKey: String = "",
    var displayName: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_character_keys", joinColumns = [JoinColumn(name = "player_id")])
    @Column(name = "character_key")
    @Fetch(FetchMode.SUBSELECT)
    var characterKeys: List<String> = emptyList(),
    var isFriend: Boolean = false,
    var isGuildMember: Boolean = false,
    /** Highest current Mythic+ score across the player's characters, as of [lastSyncedAt]. */
    var rioScore: Double? = null,
    var lastSyncedAt: Instant? = null,
    /** When this player should be refreshed next; `null` means "due immediately". */
    var nextUpdateAt: Instant? = null,
) {
    // Entities compare by persistent identity only; field-based equality would change after id assignment.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "Player(id=$id, playerKey='$playerKey')"
}
