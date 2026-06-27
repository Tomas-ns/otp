package pt.isel.domain

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
)