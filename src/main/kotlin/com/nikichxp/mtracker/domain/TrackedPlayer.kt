package com.nikichxp.mtracker.domain

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
data class TrackedPlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val displayName: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tracked_player_character_keys", joinColumns = [JoinColumn(name = "tracked_player_id")])
    @Column(name = "character_key")
    @Fetch(FetchMode.SUBSELECT)
    val characterKeys: List<String> = emptyList(),
    val isFriend: Boolean = true,
    val addedAt: Instant = Instant.now(),
)
