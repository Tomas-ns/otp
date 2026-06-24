package pt.isel.otp.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import pt.isel.otp.domain.enums.OAuthProvider
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "provider_subject"]),
    ],
)
class OAuthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    val provider: OAuthProvider,
    @Column(name = "provider_subject", nullable = false, length = 255)
    val providerSubject: String,
    @Column(length = 320)
    val providerEmail: String? = null,
    @Column(length = 255)
    val providerUsername: String? = null,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    var lastUsedAt: Instant? = null,
)
