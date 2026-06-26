package pt.isel.otp.http.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.otp.domain.dto.request.GoogleLoginRequest
import pt.isel.otp.domain.dto.request.TokenRefreshRequest
import pt.isel.otp.domain.dto.response.AuthResponse
import pt.isel.otp.service.AuthService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/oauth/google")
    fun loginWithGoogle(
        @Valid @RequestBody request: GoogleLoginRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<AuthResponse> {
        val userAgent = httpRequest.getHeader("User-Agent")
        val ipAddress = httpRequest.remoteAddr
        val response = authService.authenticateWithGoogle(request.idToken, userAgent, ipAddress)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: TokenRefreshRequest,
    ): ResponseEntity<AuthResponse> {
        val response = authService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/token")
    fun getTestToken(
        httpRequest: HttpServletRequest,
    ): AuthResponse {
        val userAgent = httpRequest.getHeader("User-Agent")
        val ipAddress = httpRequest.remoteAddr
        return authService.generateTestToken(userAgent, ipAddress)
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: TokenRefreshRequest,
    ): ResponseEntity<Unit> {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }
}
