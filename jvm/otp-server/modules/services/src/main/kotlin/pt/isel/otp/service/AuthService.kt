package pt.isel.otp.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.isel.otp.domain.dto.response.AuthResponse
import pt.isel.otp.domain.dto.response.UserResponse
import pt.isel.otp.domain.entity.OAuthAccount
import pt.isel.otp.domain.entity.User
import pt.isel.otp.domain.enums.OAuthProvider
import pt.isel.otp.domain.enums.UserStatus
import pt.isel.otp.repository.OAuthAccountRepository
import pt.isel.otp.repository.UserRepository
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    @Value("\${otp.auth.oauth.google.client-id}") private val googleClientId: String,
) {
    private val googleIdTokenVerifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
        NetHttpTransport(),
        GsonFactory(),
    )
        .setAudience(listOf(googleClientId))
        .build()

    @Transactional
    fun authenticateWithGoogle(
        idToken: String,
        userAgent: String?,
        ipAddress: String?,
    ): AuthResponse {
        val googleToken = googleIdTokenVerifier.verify(idToken)
            ?: throw IllegalArgumentException("Invalid Google ID token")

        val payload = googleToken.payload
        val subject = payload.subject
        val email = payload.email
        val name = payload.get("name") as? String
        val picture = payload.get("picture") as? String

        val existingAccount = oauthAccountRepository
            .findByProviderAndProviderSubject(OAuthProvider.GOOGLE, subject)

        val user = if (existingAccount.isPresent) {
            val account = existingAccount.get()
            val existingUser = account.user
            existingUser.lastLoginAt = Instant.now()
            existingUser.updatedAt = Instant.now()
            account.lastUsedAt = Instant.now()
            userRepository.save(existingUser)
            oauthAccountRepository.save(account)
            existingUser
        } else {
            val newUser = User(
                email = email,
                displayName = name,
                avatarUrl = picture,
                status = UserStatus.ACTIVE,
            )
            userRepository.save(newUser)

            val newAccount = OAuthAccount(
                user = newUser,
                provider = OAuthProvider.GOOGLE,
                providerSubject = subject,
                providerEmail = email,
                providerUsername = name,
            )
            oauthAccountRepository.save(newAccount)
            newUser
        }

        val userId = user.id ?: throw IllegalStateException("User ID not found")
        val accessToken = jwtService.generateAccessToken(userId)
        val (rawRefreshToken, _) = refreshTokenService.createRefreshToken(userId, userAgent, ipAddress)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            user = UserResponse(
                id = userId,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
            ),
        )
    }

    @Transactional
    fun refreshAccessToken(rawRefreshToken: String): AuthResponse {
        val (newRawToken, newTokenEntity) = refreshTokenService.rotateRefreshToken(rawRefreshToken)
        val userId = newTokenEntity.user.id ?: throw IllegalStateException("User ID not found")

        val accessToken = jwtService.generateAccessToken(userId)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = newRawToken,
            user = userRepository.findById(userId).map { u ->
                val uid = u.id ?: throw IllegalStateException("User ID not found")
                UserResponse(
                    id = uid,
                    email = u.email,
                    displayName = u.displayName,
                    avatarUrl = u.avatarUrl,
                )
            }.orElseThrow { IllegalStateException("User not found") },
        )
    }

    @Transactional
    fun logout(rawRefreshToken: String) {
        refreshTokenService.revokeRefreshToken(rawRefreshToken)
    }

    @Transactional
    fun generateTestToken(userAgent: String?, ipAddress: String?): AuthResponse {
        val user = userRepository.findById(TEST_USER_ID)
            .orElseThrow { IllegalStateException("Test user not found. Run V3 migration first.") }
        user.lastLoginAt = Instant.now()
        user.updatedAt = Instant.now()
        userRepository.save(user)

        val accessToken = jwtService.generateAccessToken(TEST_USER_ID)
        val (rawRefreshToken, _) = refreshTokenService.createRefreshToken(TEST_USER_ID, userAgent, ipAddress)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            user = UserResponse(
                id = TEST_USER_ID,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
            ),
        )
    }

    companion object {
        private val TEST_USER_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001")
    }
}
