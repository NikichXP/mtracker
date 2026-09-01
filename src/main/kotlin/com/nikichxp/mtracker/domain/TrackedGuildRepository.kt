package com.nikichxp.mtracker.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface TrackedGuildRepository : MongoRepository<TrackedGuild, String>
