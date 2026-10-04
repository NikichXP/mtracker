package com.nikichxp.mtracker.config

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.serializer.RedisSerializer

class JsonRedisSerializer<T : Any>(
    private val objectMapper: ObjectMapper,
    private val type: JavaType,
) : RedisSerializer<T> {

    override fun serialize(value: T?): ByteArray = value?.let(objectMapper::writeValueAsBytes) ?: ByteArray(0)

    override fun deserialize(bytes: ByteArray?): T? =
        bytes?.takeIf { it.isNotEmpty() }?.let { objectMapper.readValue(it, type) }
}
