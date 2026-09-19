package com.nikichxp.mtracker.service

import com.nikichxp.mtracker.config.WorkerIdentity
import com.nikichxp.mtracker.domain.jobrun.JobRunLock
import com.nikichxp.mtracker.domain.jobrun.JobRunLockRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@Service
class JobRunLockService(
    private val repository: JobRunLockRepository,
    private val workerIdentity: WorkerIdentity,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun tryAcquire(jobName: String, runDate: LocalDate, staleAfter: Duration): JobRunLock? {
        val inserted = repository.insertIfAbsent(
            JobRunLock(jobName = jobName, runDate = runDate, workerId = workerIdentity.id)
        )
        val existing = repository.findByJobNameAndRunDate(jobName, runDate) ?: return null
        if (inserted) {
            return existing
        }
        if (existing.heartbeatAt.isAfter(Instant.now().minus(staleAfter))) {
            return null
        }
        return stealStaleLock(existing)
    }

    private fun stealStaleLock(lock: JobRunLock): JobRunLock? {
        val lastHeartbeat = lock.heartbeatAt
        return try {
            lock.workerId = workerIdentity.id
            lock.heartbeatAt = Instant.now()
            val stolen = repository.saveAndFlush(lock)
            log.warn(
                "Stole stale job lock {} for {} (last heartbeat {})",
                lock.jobName, lock.runDate, lastHeartbeat,
            )
            stolen
        } catch (e: ObjectOptimisticLockingFailureException) {
            null
        } catch (e: DataIntegrityViolationException) {
            null
        }
    }

    fun tryHeartbeat(lock: JobRunLock): Boolean {
        return try {
            lock.heartbeatAt = Instant.now()
            repository.saveAndFlush(lock)
            true
        } catch (_: DataAccessException) {
            false
        }
    }

    fun releaseIfOwned(lock: JobRunLock) {
        try {
            repository.deleteByIdAndWorkerId(requireNotNull(lock.id), workerIdentity.id)
        } catch (e: Exception) {
            log.warn("Failed to release job lock {} for {}", lock.jobName, lock.runDate, e)
        }
    }
}
