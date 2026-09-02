package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.config.MtrackerProperties
import org.springframework.stereotype.Component

@Component
class S2sAuthFilter(private val props: MtrackerProperties) : TokenGuardWebFilter("/api/v1/s2s/") {
    override fun expectedToken(): String = props.s2s.token
}
