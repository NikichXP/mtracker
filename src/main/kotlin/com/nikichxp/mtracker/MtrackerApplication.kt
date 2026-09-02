package com.nikichxp.mtracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class MtrackerApplication

fun main(args: Array<String>) {
    runApplication<MtrackerApplication>(*args)
}
