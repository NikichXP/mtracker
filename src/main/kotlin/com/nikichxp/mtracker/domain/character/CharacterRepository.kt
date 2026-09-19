package com.nikichxp.mtracker.domain.character

import org.springframework.data.jpa.repository.JpaRepository

interface CharacterRepository : JpaRepository<Character, Long> {
    fun findByCharacterKey(characterKey: String): Character?
    fun findByCharacterKeyIn(keys: Collection<String>): List<Character>
}
