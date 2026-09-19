package com.nikichxp.mtracker.domain.player

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface PlayerRepository : JpaRepository<Player, Long> {
    fun findByPlayerKey(playerKey: String): Player?

    fun findByRioScoreGreaterThan(score: Double): List<Player>

    @Query("select distinct p from Player p join p.characterKeys k where k in :keys")
    fun findByCharacterKeysIn(@Param("keys") keys: Collection<String>): List<Player>

    /** Players whose update is due (never synced or [Player.nextUpdateAt] in the past), oldest first. */
    @Query(
        "select p from Player p where p.nextUpdateAt is null or p.nextUpdateAt <= :now " +
            "order by p.nextUpdateAt asc nulls first"
    )
    fun findDueForUpdate(@Param("now") now: Instant, pageable: Pageable): List<Player>
}
