package pt.isel.otp.repository

import org.springframework.data.jpa.repository.JpaRepository
import pt.isel.otp.domain.entity.OAuthAccount
import pt.isel.otp.domain.enums.OAuthProvider
import java.util.Optional
import java.util.UUID

interface OAuthAccountRepository : JpaRepository<OAuthAccount, UUID> {
    fun findByProviderAndProviderSubject(
        provider: OAuthProvider,
        providerSubject: String,
    ): Optional<OAuthAccount>
}
