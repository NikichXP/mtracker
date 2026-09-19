package com.nikichxp.mtracker.domain.topgear

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.MapKeyEnumerated
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

@Embeddable
data class PartyMemberSpec(
    val specId: Int = 0,
    val specName: String = "",
    val specSlug: String = "",
    val role: String = "",
    val className: String = "",
)

@Entity
@Table(
    name = "gear_snapshots",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_gear_snapshot_run_character",
            columnNames = ["season", "keystone_run_id", "character_key"]
        )
    ]
)
class GearSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var region: String = "",
    @Column(nullable = false)
    var season: String = "",
    @Column(name = "keystone_run_id", nullable = false)
    var keystoneRunId: Long = 0,
    @Column(name = "character_key", nullable = false)
    var characterKey: String = "",
    var name: String = "",
    var realmSlug: String = "",
    var className: String = "",
    var specId: Int = 0,
    var specName: String = "",
    var specSlug: String = "",
    var role: String = "",
    var dungeonName: String = "",
    var dungeonShortName: String? = null,
    var mythicLevel: Int = 0,
    var runScore: Double? = null,
    var timed: Boolean = false,
    var numKeystoneUpgrades: Int = 0,
    var completedAt: Instant? = null,
    var itemLevelEquipped: Double? = null,
    var rioScore: Double? = null,
    @Column(length = 2048)
    var talentImportString: String? = null,
    var capturedAt: Instant = Instant.now(),
    @OneToMany(mappedBy = "snapshot", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    var items: List<GearSnapshotItem> = emptyList(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_snapshot_party", joinColumns = [JoinColumn(name = "snapshot_id")])
    @Fetch(FetchMode.SUBSELECT)
    var partySpecs: List<PartyMemberSpec> = emptyList(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_snapshot_stats", joinColumns = [JoinColumn(name = "snapshot_id")])
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "stat")
    @Column(name = "stat_value")
    @Fetch(FetchMode.SUBSELECT)
    var stats: Map<StatType, Int> = emptyMap(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gear_snapshot_sources", joinColumns = [JoinColumn(name = "snapshot_id")])
    @Column(name = "source")
    @Enumerated(EnumType.STRING)
    @Fetch(FetchMode.SUBSELECT)
    var gearSources: Set<GearSource> = emptySet(),
) {
    // Collections stay out of equals/hashCode/toString to avoid recursion via GearSnapshotItem.snapshot.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GearSnapshot) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "GearSnapshot(id=$id, characterKey='$characterKey', keystoneRunId=$keystoneRunId)"
}

data class SpecOverviewRow(
    val specId: Int,
    val specName: String,
    val specSlug: String,
    val className: String,
    val role: String,
    val parseCount: Long,
    val characterCount: Long,
    val avgItemLevel: Double?,
)

interface GearSnapshotRepository : JpaRepository<GearSnapshot, Long> {
    fun existsBySeasonAndKeystoneRunIdAndCharacterKey(season: String, keystoneRunId: Long, characterKey: String): Boolean

    @Query("select s.id from GearSnapshot s where s.capturedAt < :cutoff")
    fun findIdsByCapturedAtBefore(@Param("cutoff") cutoff: Instant): List<Long>

    fun findBySpecId(specId: Int): List<GearSnapshot>

    @Query(
        """select new com.nikichxp.mtracker.domain.topgear.SpecOverviewRow(
        s.specId, s.specName, s.specSlug, s.className, s.role,
        count(s), count(distinct s.characterKey), avg(s.itemLevelEquipped))
        from GearSnapshot s
        group by s.specId, s.specName, s.specSlug, s.className, s.role"""
    )
    fun specOverview(): List<SpecOverviewRow>
}
