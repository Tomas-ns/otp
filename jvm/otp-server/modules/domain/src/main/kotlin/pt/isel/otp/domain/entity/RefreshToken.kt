package pt.isel.otp.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.net.InetAddress
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    val tokenHash: String,
    @Column(nullable = false)
    val expiresAt: Instant,
    var revokedAt: Instant? = null,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    var replacedBy: RefreshToken? = null,
    @Column(columnDefinition = "TEXT")
    val userAgent: String? = null,
    @Column(columnDefinition = "inet")
    val ipAddress: InetAddress? = null,
)
