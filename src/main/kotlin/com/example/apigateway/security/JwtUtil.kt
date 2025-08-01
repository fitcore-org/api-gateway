package com.example.apigateway.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.token.validity}") private val tokenValidity: Long
) {
    fun getClaims(token: String): Claims? = try {
        Jwts.parserBuilder()
            .setSigningKey(jwtSecret.toByteArray())
            .build()
            .parseClaimsJws(token)
            .body
    } catch (ex: Exception) {
        null
    }

    fun generateToken(userId: String): String {
        val now = System.currentTimeMillis()
        val expiry = Date(now + tokenValidity)
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(Date(now))
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS512, jwtSecret.toByteArray())
            .compact()
    }

    fun validateToken(token: String) {
        Jwts.parserBuilder()
            .setSigningKey(jwtSecret.toByteArray())
            .build()
            .parseClaimsJws(token)
    }
}
