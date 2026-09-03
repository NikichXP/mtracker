package com.nikichxp.mtracker.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PlayerRepository : JpaRepository<Player, Long> {
    fun findByPlayerKey(playerKey: String): Player?
}
