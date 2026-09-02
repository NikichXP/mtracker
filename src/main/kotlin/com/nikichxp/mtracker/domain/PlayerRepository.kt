package com.nikichxp.mtracker.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface PlayerRepository : MongoRepository<Player, String> {
    fun findByPlayerKey(playerKey: String): Player?
}
