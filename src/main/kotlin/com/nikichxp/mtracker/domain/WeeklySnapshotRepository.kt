package com.nikichxp.mtracker.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface WeeklySnapshotRepository : MongoRepository<WeeklySnapshot, String> {
    fun findByWeekKey(weekKey: String): List<WeeklySnapshot>
    fun findByCharacterKeyOrderByCapturedAtDesc(characterKey: String): List<WeeklySnapshot>
    fun findByWeekKeyAndCharacterKey(weekKey: String, characterKey: String): WeeklySnapshot?
}
