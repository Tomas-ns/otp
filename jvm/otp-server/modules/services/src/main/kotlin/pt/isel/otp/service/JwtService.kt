package pt.isel.otp.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class JwtService(
    @Value("\${otp.auth.jwt.secret}") private val secret: String,
    @Value("\${otp.auth.jwt.issuer}") private val issuer: String,
    @Value("\${otp.auth.jwt.access-token-ttl-minutes}") private val accessTokenTtlMinutes: Long,
) {
    private val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(userId: UUID): String {
        val now = Date()
        val expiry = Date(now.time + accessTokenTtlMinutes * 60 * 1000)
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Jws<Claims> {
        return Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
    }

    fun getUserIdFromToken(token: String): UUID {
        val claims = validateToken(token).payload
        return UUID.fromString(claims.subject)
    }
}
