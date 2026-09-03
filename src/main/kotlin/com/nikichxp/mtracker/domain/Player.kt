package com.nikichxp.mtracker.domain

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

@Entity
@Table(
    name = "players",
    indexes = [
        Index(name = "idx_players_player_key", columnList = "playerKey", unique = true)
    ]
)
data class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val playerKey: String = "",
    val displayName: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_character_keys", joinColumns = [JoinColumn(name = "player_id")])
    @Column(name = "character_key")
    @Fetch(FetchMode.SUBSELECT)
    val characterKeys: List<String> = emptyList(),
    val isFriend: Boolean = false,
    val isGuildMember: Boolean = false,
)
