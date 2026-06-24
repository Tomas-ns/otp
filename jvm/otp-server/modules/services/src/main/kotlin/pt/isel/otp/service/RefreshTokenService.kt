package pt.isel.otp.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.isel.otp.domain.entity.RefreshToken
import pt.isel.otp.domain.entity.User
import pt.isel.otp.repository.RefreshTokenRepository
import java.net.InetAddress
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${otp.auth.jwt.refresh-token-ttl-days}") private val ttlDays: Long,
) {
    private val secureRandom = SecureRandom()
    private val digest = MessageDigest.getInstance("SHA-256")

    fun createRefreshToken(
        userId: UUID,
        userAgent: String? = null,
        ipAddress: String? = null,
    ): Pair<String, RefreshToken> {
        val tokenBytes = ByteArray(32).also { secureRandom.nextBytes(it) }
        val rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
        val tokenHash = hashToken(rawToken)

        val refreshToken = RefreshToken(
            user = User(id = userId),
            tokenHash = tokenHash,
            expiresAt = Instant.now().plus(ttlDays, ChronoUnit.DAYS),
            userAgent = userAgent,
            ipAddress = ipAddress?.let { InetAddress.getByName(it) },
        )
        refreshTokenRepository.save(refreshToken)
        return rawToken to refreshToken
    }

    @Transactional
    fun rotateRefreshToken(rawToken: String): Pair<String, RefreshToken> {
        val tokenHash = hashToken(rawToken)
        val existing = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }

        if (existing.revokedAt != null) {
            throw IllegalStateException("Refresh token revoked")
        }
        if (existing.expiresAt.isBefore(Instant.now())) {
            throw IllegalStateException("Refresh token expired")
        }

        existing.revokedAt = Instant.now()

        val (newRaw, newEntity) = createRefreshToken(
            userId = existing.user.id!!,
            userAgent = existing.userAgent,
            ipAddress = existing.ipAddress?.hostAddress,
        )
        existing.replacedBy = newEntity

        refreshTokenRepository.save(existing)
        return newRaw to newEntity
    }

    @Transactional
    fun revokeRefreshToken(rawToken: String) {
        val tokenHash = hashToken(rawToken)
        val existing = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }
        existing.revokedAt = Instant.now()
        refreshTokenRepository.save(existing)
    }

    private fun hashToken(token: String): String {
        return digest.digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
