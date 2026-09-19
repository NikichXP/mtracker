package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.config.MtrackerProperties
import com.nikichxp.mtracker.domain.topgear.GearSnapshot
import com.nikichxp.mtracker.domain.topgear.GearSnapshotRepository
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTask
import com.nikichxp.mtracker.domain.topgear.TopPlayerScanTaskRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.InetAddress
import java.time.Instant
import java.util.UUID

@Component
class ScanTaskClaimer(
    private val taskRepository: TopPlayerScanTaskRepository,
    private val snapshotRepository: GearSnapshotRepository,
    private val props: MtrackerProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    val instanceId: String = resolveInstanceId()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun claim(task: TopPlayerScanTask): TopPlayerScanTask? {
        return try {
            task.claimedAt = Instant.now()
            task.claimedBy = instanceId
            task.attempts += 1
            taskRepository.saveAndFlush(task)
        } catch (e: ObjectOptimisticLockingFailureException) {
            null
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun release(task: TopPlayerScanTask) {
        try {
            task.claimedAt = null
            task.claimedBy = null
            taskRepository.saveAndFlush(task)
        } catch (e: ObjectOptimisticLockingFailureException) {
            log.debug("Failed to release scan task {}: lost lock race", task.id)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun complete(task: TopPlayerScanTask) {
        try {
            taskRepository.deleteById(requireNotNull(task.id))
        } catch (e: EmptyResultDataAccessException) {
            log.debug("Scan task {} already gone", task.id)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun abandon(task: TopPlayerScanTask) {
        try {
            taskRepository.deleteById(requireNotNull(task.id))
            log.error(
                "Abandoned scan task {}-{} spec {} after {} attempts",
                task.name, task.realmSlug, task.specName, task.attempts,
            )
        } catch (e: EmptyResultDataAccessException) {
            log.debug("Scan task {} already gone", task.id)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveSnapshot(snapshot: GearSnapshot): Boolean {
        return try {
            snapshotRepository.saveAndFlush(snapshot)
            true
        } catch (e: DataIntegrityViolationException) {
            false
        }
    }

    private fun resolveInstanceId(): String {
        val fromEnv = System.getenv("HOSTNAME")
        if (!fromEnv.isNullOrBlank()) {
            return fromEnv
        }
        return try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "instance-${UUID.randomUUID()}"
        }
    }
}
