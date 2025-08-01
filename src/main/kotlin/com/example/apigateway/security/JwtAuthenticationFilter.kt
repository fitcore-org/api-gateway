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

    private val excludedPaths = listOf("/auth/login", "/auth/register")

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val requestPath = exchange.request.path.toString()
        if (excludedPaths.any { requestPath.startsWith(it) }) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst("Authorization")
        if (authHeader.isNullOrBlank()) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }

        val token = authHeader
        return try {
            jwtUtil.validateToken(token)
            val claims = jwtUtil.getClaims(token)
            val userId = claims?.subject
            val mutated = exchange.request.mutate()
            if (!userId.isNullOrBlank()) {
                mutated.header("X-User-Id", userId)
            }
            val newRequest = mutated.build()
            chain.filter(exchange.mutate().request(newRequest).build())
        } catch (ex: Exception) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            exchange.response.setComplete()
        }
    }

    override fun getOrder(): Int = -1
}
