package com.nikichxp.mtracker.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface CharacterRepository : MongoRepository<Character, String> {
    fun findByCharacterKey(characterKey: String): Character?
    fun findByCharacterKeyIn(keys: Collection<String>): List<Character>
}
