package pt.isel.otp.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.isel.otp.domain.entity.OAuthAccount
import pt.isel.otp.domain.entity.User
import pt.isel.otp.domain.enums.OAuthProvider

@DataJpaTest
class OAuthAccountRepositoryTest {
    @Autowired
    private lateinit var oauthAccountRepository: OAuthAccountRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user: User

    @BeforeEach
    fun setup() {
        user = userRepository.save(User(email = "oauth@test.com"))
    }

    @Test
    fun `save and find by provider and subject`() {
        val account = OAuthAccount(user = user, provider = OAuthProvider.GOOGLE, providerSubject = "google-123")
        oauthAccountRepository.save(account)
        val found = oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-123")
        assertTrue(found.isPresent)
        assertEquals(user.id, found.get().user.id)
    }

    @Test
    fun `findByProviderAndProviderSubject returns empty for unknown subject`() {
        val found = oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "unknown")
        assertFalse(found.isPresent)
    }

    @Test
    fun `findByProviderAndProviderSubject returns empty for wrong provider`() {
        val account = OAuthAccount(user = user, provider = OAuthProvider.GOOGLE, providerSubject = "sub")
        oauthAccountRepository.save(account)
        val found = oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "other")
        assertFalse(found.isPresent)
    }

    @Test
    fun `save with provider email and username`() {
        val account = OAuthAccount(
            user = user,
            provider = OAuthProvider.GOOGLE,
            providerSubject = "sub-with-details",
            providerEmail = "google@test.com",
            providerUsername = "GoogleUser",
        )
        val saved = oauthAccountRepository.save(account)
        assertEquals("google@test.com", saved.providerEmail)
        assertEquals("GoogleUser", saved.providerUsername)
    }

    @Test
    fun `unique constraint on provider and providerSubject`() {
        oauthAccountRepository.save(OAuthAccount(user = user, provider = OAuthProvider.GOOGLE, providerSubject = "dup"))
        assertThrows(org.springframework.dao.DataIntegrityViolationException::class.java) {
            oauthAccountRepository.save(OAuthAccount(user = user, provider = OAuthProvider.GOOGLE, providerSubject = "dup"))
            oauthAccountRepository.flush()
        }
    }
}
