package pt.isel.otp.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtServiceTest {
    private lateinit var jwtService: JwtService
    private val secret = "this-is-a-test-secret-that-is-exactly-256-bits-long-for-hmac!!"
    private val issuer = "test-issuer"
    private val ttlMinutes = 15L

    @BeforeEach
    fun setup() {
        jwtService = JwtService(secret, issuer, ttlMinutes)
    }

    @Test
    fun `generateAccessToken produces non-empty token`() {
        val token = jwtService.generateAccessToken(UUID.randomUUID())
        assertFalse(token.isBlank())
        assertTrue(token.split(".").size == 3)
    }

    @Test
    fun `validateToken succeeds for valid token`() {
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId)
        val claims = jwtService.validateToken(token)
        assertEquals(userId.toString(), claims.payload.subject)
        assertEquals(issuer, claims.payload.issuer)
    }

    @Test
    fun `getUserIdFromToken returns correct UUID`() {
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId)
        val extracted = jwtService.getUserIdFromToken(token)
        assertEquals(userId, extracted)
    }

    @Test
    fun `validateToken fails for token with wrong issuer`() {
        val otherService = JwtService(secret, "wrong-issuer", ttlMinutes)
        val token = otherService.generateAccessToken(UUID.randomUUID())
        assertThrows(Exception::class.java) {
            jwtService.validateToken(token)
        }
    }

    @Test
    fun `validateToken fails for token with different secret`() {
        val otherService = JwtService("different-secret-that-is-also-256-bits-long-for-hmac-test!!", issuer, ttlMinutes)
        val token = otherService.generateAccessToken(UUID.randomUUID())
        assertThrows(Exception::class.java) { jwtService.validateToken(token) }
    }

    @Test
    fun `validateToken fails for malformed token`() {
        assertThrows(Exception::class.java) { jwtService.validateToken("not.a.token") }
    }

    @Test
    fun `validateToken fails for empty token`() {
        assertThrows(Exception::class.java) { jwtService.validateToken("") }
    }

    @Test
    fun `multiple tokens can be generated and validated`() {
        val ids = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        val tokens = ids.map { jwtService.generateAccessToken(it) }
        tokens.forEachIndexed { i, token ->
            val extracted = jwtService.getUserIdFromToken(token)
            assertEquals(ids[i], extracted)
        }
    }

    @Test
    fun `token contains correct issuer in claims`() {
        val token = jwtService.generateAccessToken(UUID.randomUUID())
        val claims = jwtService.validateToken(token)
        assertEquals(issuer, claims.payload.issuer)
    }

    @Test
    fun `token has expiration in the future`() {
        val token = jwtService.generateAccessToken(UUID.randomUUID())
        val claims = jwtService.validateToken(token)
        assertTrue(claims.payload.expiration.time > System.currentTimeMillis())
    }

    @Test
    fun `token has issued-at time`() {
        val token = jwtService.generateAccessToken(UUID.randomUUID())
        val claims = jwtService.validateToken(token)
        assertNotNull(claims.payload.issuedAt)
    }
}
