package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * A real person, grouping a main character with its alts. Alt-linking is config-driven
 * (see [com.nikichxp.mtracker.sync.PlayerLinkService]) since Raider.io exposes no such relation.
 */
@Document(collection = "players")
data class Player(
    @Id val id: String? = null,
    @Indexed(unique = true) val playerKey: String,
    val displayName: String,
    val characterKeys: List<String>,
    val isFriend: Boolean,
    val isGuildMember: Boolean,
)
