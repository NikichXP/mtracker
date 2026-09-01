package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.config.MtrackerProperties
import org.springframework.stereotype.Component

/**
 * Guards the `/api/v1/s2s` routes with a shared-secret bearer token (configured via
 * [MtrackerProperties.S2s] / `MTRACKER_S2S_TOKEN`), so other in-house services (e.g. tg-bot) can call it
 * without exposing the data to arbitrary callers. Does not affect the public `/api/v1/stats`
 * dashboard endpoints.
 */
@Component
class S2sAuthFilter(private val props: MtrackerProperties) : TokenGuardWebFilter("/api/v1/s2s/") {
    override fun expectedToken(): String = props.s2s.token
}
