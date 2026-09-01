package com.nikichxp.mtracker.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.security.MessageDigest

/**
 * Guards every request under [pathPrefix] with a shared-secret bearer token. Fails closed: if
 * [expectedToken] is blank, every matching request is rejected rather than silently left open.
 */
abstract class TokenGuardWebFilter(private val pathPrefix: String) : WebFilter {

    private val log = LoggerFactory.getLogger(javaClass)

    protected abstract fun expectedToken(): String

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        if (!path.startsWith(pathPrefix)) {
            return chain.filter(exchange)
        }

        val expectedToken = expectedToken()
        if (expectedToken.isBlank()) {
            log.warn("Rejected request to {}: required token is not configured", path)
            return reject(exchange)
        }

        val header = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val presentedToken = header?.removePrefix(BEARER_PREFIX)?.takeIf { header.startsWith(BEARER_PREFIX) }

        if (presentedToken == null || !constantTimeEquals(presentedToken, expectedToken)) {
            log.warn("Rejected request to {}: missing or invalid bearer token", path)
            return reject(exchange)
        }

        return chain.filter(exchange)
    }

    private fun reject(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response.setComplete()
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
