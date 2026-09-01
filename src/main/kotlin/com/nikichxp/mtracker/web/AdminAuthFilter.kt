package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.config.MtrackerProperties
import org.springframework.stereotype.Component

@Component
class AdminAuthFilter(private val props: MtrackerProperties) : TokenGuardWebFilter("/api/v1/admin/") {
    override fun expectedToken(): String = props.admin.token
}
