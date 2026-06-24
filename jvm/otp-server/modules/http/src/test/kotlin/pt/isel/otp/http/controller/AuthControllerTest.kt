package pt.isel.otp.http.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import pt.isel.otp.domain.dto.response.AuthResponse
import pt.isel.otp.domain.dto.response.UserResponse
import pt.isel.otp.service.AuthService
import java.util.UUID

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

    @Test
    fun `POST oauth google returns 201`() {
        val userId = UUID.randomUUID()
        val authResponse = AuthResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            user = UserResponse(id = userId, email = "u@u.com", displayName = "U", avatarUrl = null),
        )
        `when`(authService.authenticateWithGoogle(anyString(), any(), any())).thenReturn(authResponse)
        mockMvc.perform(
            post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"idToken":"valid-token"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.user.id").value(userId.toString()))
    }

    @Test
    fun `POST oauth google returns 400 for blank idToken`() {
        mockMvc.perform(
            post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"idToken":""}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST oauth google returns 400 for missing body`() {
        mockMvc.perform(
            post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST oauth google returns 400 for invalid JSON`() {
        mockMvc.perform(
            post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not json")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST refresh returns 200`() {
        val authResponse = AuthResponse(
            accessToken = "new-at",
            refreshToken = "new-rt",
            user = UserResponse(id = UUID.randomUUID(), email = "u@u.com", displayName = null, avatarUrl = null),
        )
        `when`(authService.refreshAccessToken("valid-rt")).thenReturn(authResponse)
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"valid-rt"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-at"))
    }

    @Test
    fun `POST refresh returns 400 for blank token`() {
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":""}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST logout returns 204`() {
        doNothing().`when`(authService).logout("valid-rt")
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"valid-rt"}""")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `POST logout returns 400 for blank token`() {
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":""}""")
        )
            .andExpect(status().isBadRequest)
    }
}
