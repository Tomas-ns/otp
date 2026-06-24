package pt.isel.otp.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import pt.isel.otp.domain.entity.RefreshToken
import pt.isel.otp.domain.entity.User
import pt.isel.otp.repository.RefreshTokenRepository
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class RefreshTokenServiceTest {
    @Mock private lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var refreshTokenService: RefreshTokenService
    private val ttlDays = 30L
    private val userId = UUID.randomUUID()
    private val user = User(id = userId)

    @BeforeEach
    fun setup() {
        refreshTokenService = RefreshTokenService(refreshTokenRepository, ttlDays)
    }

    @Test
    fun `createRefreshToken returns raw token and entity`() {
        val (raw, entity) = refreshTokenService.createRefreshToken(userId)
        assertFalse(raw.isBlank())
        assertEquals(userId, entity.user.id)
        assertNotNull(entity.expiresAt)
        assertTrue(entity.expiresAt.isAfter(Instant.now()))
    }

    @Test
    fun `createRefreshToken persists entity`() {
        refreshTokenService.createRefreshToken(userId)
        verify(refreshTokenRepository).save(any<RefreshToken>())
    }

    @Test
    fun `createRefreshToken with user agent and ip`() {
        val (raw, entity) = refreshTokenService.createRefreshToken(userId, "Chrome", "127.0.0.1")
        assertEquals("Chrome", entity.userAgent)
        assertEquals(java.net.InetAddress.getByName("127.0.0.1"), entity.ipAddress)
    }

    @Test
    fun `rotateRefreshToken revokes old and creates new`() {
        val (oldRaw, oldEntity) = refreshTokenService.createRefreshToken(userId, "Mozilla", "10.0.0.1")
        val oldHash = sha256(oldRaw)
        `when`(refreshTokenRepository.findByTokenHash(oldHash)).thenReturn(Optional.of(oldEntity))
        `when`(refreshTokenRepository.save(any<RefreshToken>())).thenAnswer { it.arguments[0] }
        val (newRaw, newEntity) = refreshTokenService.rotateRefreshToken(oldRaw)
        assertNotNull(oldEntity.revokedAt)
        assertEquals(newEntity, oldEntity.replacedBy)
        assertNotEquals(oldRaw, newRaw)
    }

    @Test
    fun `rotateRefreshToken throws for invalid token`() {
        `when`(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty())
        assertThrows(IllegalArgumentException::class.java) {
            refreshTokenService.rotateRefreshToken("invalid-token")
        }
    }

    @Test
    fun `rotateRefreshToken throws for revoked token`() {
        val (raw, entity) = refreshTokenService.createRefreshToken(userId)
        entity.revokedAt = Instant.now()
        val hash = sha256(raw)
        `when`(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(entity))
        assertThrows(IllegalStateException::class.java) {
            refreshTokenService.rotateRefreshToken(raw)
        }
    }

    @Test
    fun `rotateRefreshToken throws for expired token`() {
        val expiredEntity = RefreshToken(user = user, tokenHash = "hash", expiresAt = Instant.now().minusSeconds(1))
        val raw = "some-raw-token"
        val hash = sha256(raw)
        `when`(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expiredEntity))
        assertThrows(IllegalStateException::class.java) {
            refreshTokenService.rotateRefreshToken(raw)
        }
    }

    @Test
    fun `revokeRefreshToken marks token as revoked`() {
        val (raw, entity) = refreshTokenService.createRefreshToken(userId)
        val hash = sha256(raw)
        `when`(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(entity))
        refreshTokenService.revokeRefreshToken(raw)
        assertNotNull(entity.revokedAt)
        verify(refreshTokenRepository, times(2)).save(any())
    }

    @Test
    fun `revokeRefreshToken throws for invalid token`() {
        `when`(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty())
        assertThrows(IllegalArgumentException::class.java) {
            refreshTokenService.revokeRefreshToken("invalid")
        }
    }

    @Test
    fun `createRefreshToken generates different tokens each time`() {
        val (raw1, _) = refreshTokenService.createRefreshToken(userId)
        val (raw2, _) = refreshTokenService.createRefreshToken(userId)
        assertNotEquals(raw1, raw2)
    }

    private fun sha256(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
