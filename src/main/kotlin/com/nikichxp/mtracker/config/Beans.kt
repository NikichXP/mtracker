package com.nikichxp.mtracker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class Beans(private val props: MtrackerProperties) {

    @Bean
    fun objectMapper() = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Bean
    fun raiderIoWebClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl(props.raiderio.baseUrl).build()
}
