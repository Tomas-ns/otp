package pt.isel.otp.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import pt.isel.otp.domain.dto.response.AuthResponse
import pt.isel.otp.domain.entity.OAuthAccount
import pt.isel.otp.domain.entity.User
import pt.isel.otp.domain.enums.OAuthProvider
import pt.isel.otp.domain.enums.UserStatus
import pt.isel.otp.repository.OAuthAccountRepository
import pt.isel.otp.repository.UserRepository
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var oauthAccountRepository: OAuthAccountRepository
    @Mock private lateinit var jwtService: JwtService
    @Mock private lateinit var refreshTokenService: RefreshTokenService

    private lateinit var authService: AuthService
    private val googleClientId = "test-client-id.apps.googleusercontent.com"

    @BeforeEach
    fun setup() {
        authService = AuthService(userRepository, oauthAccountRepository, jwtService, refreshTokenService, googleClientId)
    }

    @Test
    fun `authenticateWithGoogle throws for invalid token`() {
        assertThrows(IllegalArgumentException::class.java) {
            authService.authenticateWithGoogle("invalid-id-token", null, null)
        }
    }

    @Test
    fun `refreshAccessToken returns AuthResponse with new tokens`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, email = "test@test.com", displayName = "Test", status = UserStatus.ACTIVE)
        val newRawToken = "new-refresh-token"
        val newTokenEntity = pt.isel.otp.domain.entity.RefreshToken(
            user = user, tokenHash = "hash", expiresAt = java.time.Instant.now().plusSeconds(86400)
        )
        `when`(refreshTokenService.rotateRefreshToken("old-refresh-token")).thenReturn(newRawToken to newTokenEntity)
        `when`(jwtService.generateAccessToken(userId)).thenReturn("new-access-token")
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        val response = authService.refreshAccessToken("old-refresh-token")
        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals(userId, response.user.id)
        assertEquals("test@test.com", response.user.email)
    }

    @Test
    fun `refreshAccessToken throws when user not found`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId)
        `when`(refreshTokenService.rotateRefreshToken("token")).thenReturn(
            "new-raw" to pt.isel.otp.domain.entity.RefreshToken(
                user = user, tokenHash = "h", expiresAt = java.time.Instant.now().plusSeconds(86400)
            )
        )
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())
        assertThrows(IllegalStateException::class.java) {
            authService.refreshAccessToken("token")
        }
    }

    @Test
    fun `logout calls revokeRefreshToken`() {
        authService.logout("some-refresh-token")
        verify(refreshTokenService).revokeRefreshToken("some-refresh-token")
    }

    @Test
    fun `logout with empty token calls revoke`() {
        authService.logout("")
        verify(refreshTokenService).revokeRefreshToken("")
    }
}
