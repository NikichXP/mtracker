package com.nikichxp.mtracker.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "weekly_snapshots")
@CompoundIndex(def = "{'weekKey': 1, 'characterKey': 1}", unique = true)
data class WeeklySnapshot(
    @Id val id: String? = null,
    val weekKey: String,
    val characterKey: String,
    val mythicPlusScore: Double?,
    val weeklyRunsCount: Int,
    val weeklyHighestLevel: Int,
    val capturedAt: Instant,
)
