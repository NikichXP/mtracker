package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.config.MtrackerProperties
import org.springframework.stereotype.Component

/**
 * Guards the `/api/v1/admin` CRUD routes (tracked guilds/players management) with a
 * shared-secret bearer token, separate from [S2sAuthFilter]'s token, since it grants
 * write access rather than just read access to internal services.
 */
@Component
class AdminAuthFilter(private val props: MtrackerProperties) : TokenGuardWebFilter("/api/v1/admin/") {
    override fun expectedToken(): String = props.admin.token
}
