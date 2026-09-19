package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.topgear.GearSnapshotRepository
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class TopGearCleanupService(
    private val snapshotRepository: GearSnapshotRepository,
    private val taskRepository: TopPlayerScanTaskRepository,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun purgeDeprecated() {
        val cutoff = Instant.now().minus(props.topGear.retentionHours, ChronoUnit.HOURS)
        val snapshotIds = snapshotRepository.findIdsByCapturedAtBefore(cutoff)
        snapshotIds.chunked(500).forEach { chunk ->
            snapshotRepository.deleteAll(snapshotRepository.findAllById(chunk))
        }
        val taskCount = taskRepository.deleteByCreatedAtBefore(cutoff)
        log.info("Top gear cleanup: removed {} gear snapshots and {} scan tasks", snapshotIds.size, taskCount)
    }
}
