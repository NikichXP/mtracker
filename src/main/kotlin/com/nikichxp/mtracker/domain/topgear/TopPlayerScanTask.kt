package com.nikichxp.mtracker.domain.topgear

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

@Entity
@Table(
    name = "top_player_scan_tasks",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_scan_task_season_region_key_spec",
            columnNames = ["season", "region", "character_key", "spec_id"]
        )
    ]
)
class TopPlayerScanTask(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var region: String = "",
    @Column(nullable = false)
    var season: String = "",
    @Column(name = "character_key", nullable = false)
    var characterKey: String = "",
    var name: String = "",
    var realmSlug: String = "",
    var className: String = "",
    @Column(name = "spec_id")
    var specId: Int = 0,
    var specName: String = "",
    var specSlug: String = "",
    var role: String = "",
    var rioScore: Double = 0.0,
    var rank: Int = 0,
    @Column(length = 2048)
    var talentImportString: String? = null,
    var guildName: String? = null,
    var createdAt: Instant = Instant.now(),
    var claimedAt: Instant? = null,
    var claimedBy: String? = null,
    var attempts: Int = 0,
    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TopPlayerScanTask) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "TopPlayerScanTask(id=$id, characterKey='$characterKey', specId=$specId)"
}

interface TopPlayerScanTaskRepository : JpaRepository<TopPlayerScanTask, Long> {
    fun existsBySeasonAndRegionAndCharacterKeyAndSpecId(
        season: String, region: String, characterKey: String, specId: Int
    ): Boolean

    fun findBySeasonAndRegionAndCharacterKeyAndSpecId(
        season: String, region: String, characterKey: String, specId: Int
    ): TopPlayerScanTask?

    @Query("select t from TopPlayerScanTask t where t.claimedAt is null or t.claimedAt < :staleBefore order by t.rioScore desc")
    fun findClaimable(@Param("staleBefore") staleBefore: Instant, pageable: Pageable): List<TopPlayerScanTask>

    fun deleteByCreatedAtBefore(cutoff: Instant): Long

    fun countBySeasonAndRegion(season: String, region: String): Long
}
