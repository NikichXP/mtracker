package com.nikichxp.mtracker.domain.jobrun

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.transaction.annotation.Transactional

interface JobRunLockCustomRepository {
    fun insertIfAbsent(lock: JobRunLock): Boolean
}

open class JobRunLockCustomRepositoryImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : JobRunLockCustomRepository {

    @Transactional
    override fun insertIfAbsent(lock: JobRunLock): Boolean {
        val inserted = entityManager.createNativeQuery(
            """
            insert into job_run_locks (job_name, run_date, worker_id, started_at, heartbeat_at, version)
            values (:jobName, :runDate, :workerId, :startedAt, :heartbeatAt, 0)
            on conflict (job_name, run_date) do nothing
            """.trimIndent(),
        )
            .setParameter("jobName", lock.jobName)
            .setParameter("runDate", lock.runDate)
            .setParameter("workerId", lock.workerId)
            .setParameter("startedAt", lock.startedAt)
            .setParameter("heartbeatAt", lock.heartbeatAt)
            .executeUpdate()
        return inserted > 0
    }
}
