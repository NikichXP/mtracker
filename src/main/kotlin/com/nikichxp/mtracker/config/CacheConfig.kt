package com.nikichxp.mtracker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.mtracker.web.dto.PlayerOverviewDto
import org.slf4j.LoggerFactory
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig : CachingConfigurer {

    @Bean
    fun redisCacheCustomizer(objectMapper: ObjectMapper): RedisCacheManagerBuilderCustomizer {
        val overviewType = objectMapper.typeFactory.constructCollectionType(List::class.java, PlayerOverviewDto::class.java)
        return RedisCacheManagerBuilderCustomizer { builder ->
            builder.withCacheConfiguration(
                STATS_OVERVIEW,
                RedisCacheConfiguration.defaultCacheConfig()
                    .prefixCacheNameWith("mtracker:")
                    .entryTtl(Duration.ofMinutes(5))
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                            JsonRedisSerializer<List<PlayerOverviewDto>>(objectMapper, overviewType)
                        )
                    )
            )
        }
    }

    override fun errorHandler(): CacheErrorHandler = LoggingCacheErrorHandler()

    private class LoggingCacheErrorHandler : CacheErrorHandler {
        private val log = LoggerFactory.getLogger(javaClass)

        override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) =
            log.warn("Cache get failed for {}::{}: {}", cache.name, key, exception.message)

        override fun handleCachePutError(exception: RuntimeException, cache: Cache, key: Any, value: Any?) =
            log.warn("Cache put failed for {}::{}: {}", cache.name, key, exception.message)

        override fun handleCacheEvictError(exception: RuntimeException, cache: Cache, key: Any) =
            log.warn("Cache evict failed for {}::{}: {}", cache.name, key, exception.message)

        override fun handleCacheClearError(exception: RuntimeException, cache: Cache) =
            log.warn("Cache clear failed for {}: {}", cache.name, exception.message)
    }

    companion object {
        const val STATS_OVERVIEW = "stats-overview"
    }
}
