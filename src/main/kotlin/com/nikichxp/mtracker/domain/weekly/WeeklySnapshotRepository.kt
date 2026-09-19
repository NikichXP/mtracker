package com.nikichxp.mtracker.domain.weekly

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WeeklySnapshotRepository : JpaRepository<WeeklySnapshot, Long> {
    fun findByWeekKey(weekKey: String): List<WeeklySnapshot>
    fun findByCharacterKeyOrderByCapturedAtDesc(characterKey: String): List<WeeklySnapshot>
    fun findByWeekKeyAndCharacterKey(weekKey: String, characterKey: String): WeeklySnapshot?

    @Query("SELECT DISTINCT s.weekKey FROM WeeklySnapshot s ORDER BY s.weekKey DESC")
    fun findDistinctWeekKeys(): List<String>
}
