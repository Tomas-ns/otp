package pt.isel.otp.http.dto

data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
)
