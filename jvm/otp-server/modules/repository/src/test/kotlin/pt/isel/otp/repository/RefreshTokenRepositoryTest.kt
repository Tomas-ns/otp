package pt.isel.otp.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.isel.otp.domain.entity.RefreshToken
import pt.isel.otp.domain.entity.User
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@DataJpaTest
class RefreshTokenRepositoryTest {
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user: User

    @BeforeEach
    fun setup() {
        user = userRepository.save(User(email = "rt@test.com"))
    }

    @Test
    fun `save and find by token hash`() {
        val hash = sha256("test-token")
        val rt = RefreshToken(user = user, tokenHash = hash, expiresAt = Instant.now().plusSeconds(86400))
        refreshTokenRepository.save(rt)
        val found = refreshTokenRepository.findByTokenHash(hash)
        assertTrue(found.isPresent)
        assertEquals(hash, found.get().tokenHash)
    }

    @Test
    fun `findByTokenHash returns empty for unknown hash`() {
        val found = refreshTokenRepository.findByTokenHash("nonexistent")
        assertFalse(found.isPresent)
    }

    @Test
    fun `token hash unique constraint`() {
        val hash = sha256("unique-token")
        refreshTokenRepository.save(RefreshToken(user = user, tokenHash = hash, expiresAt = Instant.now().plusSeconds(86400)))
        assertThrows(org.springframework.dao.DataIntegrityViolationException::class.java) {
            refreshTokenRepository.save(RefreshToken(user = user, tokenHash = hash, expiresAt = Instant.now().plusSeconds(86400)))
            refreshTokenRepository.flush()
        }
    }

    @Test
    fun `save with revokedAt`() {
        val hash = sha256("revoked-token")
        val rt = RefreshToken(user = user, tokenHash = hash, expiresAt = Instant.now().plusSeconds(86400))
        rt.revokedAt = Instant.now()
        val saved = refreshTokenRepository.save(rt)
        assertNotNull(saved.revokedAt)
    }

    @Test
    fun `save with ip address and user agent`() {
        val hash = sha256("full-token")
        val rt = RefreshToken(
            user = user,
            tokenHash = hash,
            expiresAt = Instant.now().plusSeconds(86400),
            userAgent = "Mozilla/5.0",
            ipAddress = java.net.InetAddress.getByName("10.0.0.1"),
        )
        val saved = refreshTokenRepository.save(rt)
        assertEquals("Mozilla/5.0", saved.userAgent)
        assertEquals(java.net.InetAddress.getByName("10.0.0.1"), saved.ipAddress)
    }

    @Test
    fun `save without optional fields`() {
        val hash = sha256("minimal-token")
        val rt = RefreshToken(user = user, tokenHash = hash, expiresAt = Instant.now().plusSeconds(86400))
        val saved = refreshTokenRepository.save(rt)
        assertNull(saved.userAgent)
        assertNull(saved.ipAddress)
    }

    private fun sha256(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
