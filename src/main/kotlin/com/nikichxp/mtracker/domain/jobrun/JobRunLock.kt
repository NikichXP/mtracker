package com.nikichxp.mtracker.domain.jobrun

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "job_run_locks",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_job_run_lock_name_date",
            columnNames = ["job_name", "run_date"],
        )
    ],
)
class JobRunLock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "job_name", nullable = false)
    var jobName: String = "",
    @Column(name = "run_date", nullable = false)
    var runDate: LocalDate = LocalDate.now(),
    @Column(nullable = false)
    var workerId: String = "",
    @Column(nullable = false)
    var startedAt: Instant = Instant.now(),
    @Column(nullable = false)
    var heartbeatAt: Instant = Instant.now(),
    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JobRunLock) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "JobRunLock(id=$id, jobName='$jobName', runDate=$runDate, workerId='$workerId')"
}

interface JobRunLockRepository : JpaRepository<JobRunLock, Long>, JobRunLockCustomRepository {
    fun findByJobNameAndRunDate(jobName: String, runDate: LocalDate): JobRunLock?

    fun deleteByIdAndWorkerId(id: Long, workerId: String): Long
}
