package com.example.apigateway.config

import com.example.apigateway.security.JwtAuthenticationFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator =
        builder.routes()
            .route("auth-service") { r: PredicateSpec ->
                r.path("/auth/**")
                    .filters { f -> f.filter(jwtAuthenticationFilter) }
                    .uri("lb://AUTH-SERVICE")
            }

            .route("payment-service") { r: PredicateSpec ->
                r.path("/payment/**")
                    .filters { f -> f.filter(jwtAuthenticationFilter) }
                    .uri("lb://PAYMENT-SERVICE")
            }
            .build()
}