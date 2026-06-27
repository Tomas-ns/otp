package pt.isel.domain

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
)
