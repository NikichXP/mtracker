package com.nikichxp.mtracker.domain

import org.springframework.data.jpa.repository.JpaRepository

interface TrackedGuildRepository : JpaRepository<TrackedGuild, Long>
