package com.nikichxp.mtracker.domain.tracking

import org.springframework.data.jpa.repository.JpaRepository

interface TrackedPlayerRepository : JpaRepository<TrackedPlayer, Long>
