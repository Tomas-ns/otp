package pt.isel.otp.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import pt.isel.otp.domain.enums.UserStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(length = 320)
    val email: String? = null,
    @Column(length = 255)
    val displayName: String? = null,
    @Column(columnDefinition = "TEXT")
    val avatarUrl: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val status: UserStatus = UserStatus.ACTIVE,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
    var lastLoginAt: Instant? = null,
)
