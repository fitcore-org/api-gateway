package com.example.apigateway.security

import io.jsonwebtoken.Claims
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : GatewayFilter, Ordered {

    private val excludedPaths = listOf("/auth/login", "/auth/register", "/payment/api/plans")

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val requestPath = exchange.request.path.toString()

        if (excludedPaths.any { requestPath.startsWith(it) }) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        return try {
            jwtUtil.validateToken(token)
            val claims = jwtUtil.getClaims(token)
            val userId = claims?.subject
            val userRole = claims?.get("role", String::class.java)

            val mutatedRequest = exchange.request.mutate().apply {
                if (!userId.isNullOrBlank()) header("X-User-Id", userId)
                if (!userRole.isNullOrBlank()) header("X-User-Role", userRole)
            }.build()

            val mutatedExchange = exchange.mutate().request(mutatedRequest).build()
            chain.filter(mutatedExchange)
        } catch (ex: Exception) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.setComplete()
        }
    }

    override fun getOrder(): Int = -1
}
