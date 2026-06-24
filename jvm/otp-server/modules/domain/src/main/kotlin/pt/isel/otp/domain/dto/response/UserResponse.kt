package pt.isel.otp.domain.dto.response

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
)
