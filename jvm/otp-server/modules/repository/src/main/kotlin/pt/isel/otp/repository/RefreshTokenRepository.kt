package pt.isel.otp.repository

import org.springframework.data.jpa.repository.JpaRepository
import pt.isel.otp.domain.entity.RefreshToken
import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>
}
