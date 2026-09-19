package com.nikichxp.mtracker.domain.character

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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

enum class CharacterSource { GUILD, FRIEND, ALT }

@Entity
@Table(
    name = "characters",
    indexes = [
        Index(name = "idx_characters_character_key", columnList = "characterKey", unique = true)
    ]
)
class Character(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var characterKey: String = "",
    var name: String = "",
    var realm: String = "",
    var region: String = "",
    var characterClass: String? = null,
    var race: String? = null,
    var faction: String? = null,
    var activeSpecName: String? = null,
    var activeSpecRole: String? = null,
    var guildName: String? = null,
    var itemLevelEquipped: Double? = null,
    var mythicPlusScore: Double? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "character_weekly_runs", joinColumns = [JoinColumn(name = "character_id")])
    @Fetch(FetchMode.SUBSELECT)
    @JvmSuppressWildcards
    var weeklyRuns: List<DungeonRun> = emptyList(),
    @Enumerated(EnumType.STRING)
    var source: CharacterSource = CharacterSource.GUILD,
    var lastSyncedAt: Instant? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Character) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "Character(id=$id, characterKey='$characterKey')"
}
