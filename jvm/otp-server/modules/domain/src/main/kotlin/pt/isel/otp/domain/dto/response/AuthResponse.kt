package pt.isel.otp.domain.dto.response

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
)
