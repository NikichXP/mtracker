package com.nikichxp.mtracker.config

import org.springframework.stereotype.Component
import java.net.InetAddress
import java.util.UUID

@Component
class WorkerIdentity {

    val id: String = resolve()

    private fun resolve(): String {
        val fromEnv = System.getenv("HOSTNAME")
        if (!fromEnv.isNullOrBlank()) {
            return fromEnv
        }
        return try {
            InetAddress.getLocalHost().hostName
        } catch (_: Exception) {
            "instance-${UUID.randomUUID()}"
        }
    }
}
